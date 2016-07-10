package com.seismic

import java.io.File

import com.fasterxml.jackson.annotation.{JsonBackReference, JsonManagedReference}
import com.seismic.messages.TriggerOnMessage
import com.seismic.midi.{MIDIIO, MidiNoteMap}
import com.seismic.ui.swing.Selectable
import com.seismic.utils.{Next, SetListSerializer}
import com.seismic.utils.ValueMapHelper.map
import com.seismic.utils.ArrayUtils.wrapIndex
import com.seismic.scala.OptionExtensions._
import com.seismic.utils.Next.next
import processing.core.PApplet.constrain

/**
  * Contains the structure and management of a SetList of Songs and MIDIInstruments
  * TODO: should I rename "phrase" to "scene" to match Maschine?
  *
  * @param midiIO
  */
class Seismic(midiIO: MIDIIO) {

  val triggeredState = new TriggeredState
  var setListOpt: Option[SetList] = None
  var currentSongOpt: Option[Song] = None
  var currentPhraseOpt: Option[Phrase] = None

  def openSetList(file: File) = {
    val setList = SetListSerializer.read(file)
    setListOpt = Option(setList)
    currentSongOpt = Option(setList.songs.head)
    currentPhraseOpt = Option(setList.songs.head.phrases.head)
    setList
  }

  def save(): Unit = {
    setListOpt.foreach { setlist => setlist.write() }
  }

  def setCurrentSong(song: Song): Unit = {
    currentSongOpt = Option(song)
  }

  def setCurrentPhrase(phrase: Phrase): Unit = {
    currentPhraseOpt = Option(phrase)
  }

  def selectNextPhrase() = {
    indexOfCurrentPhrase().flatMap { index => selectPhraseAt(index + 1) }
  }

  def selectPreviousPhrase() = {
    indexOfCurrentPhrase().flatMap { index => selectPhraseAt(index - 1) }
  }

  private def selectPhraseAt(index: Int): Option[Phrase] = {
    currentSongOpt.flatMap { song =>
      if (song.phrases.length == index) {
        selectFirstPhraseInNextSong()
      } else if (index < 0) {
        selectLastPhraseFromPreviousSong()
      } else {
        val newPhrase = song.phrases(wrapIndex(index, song.phrases))
        setCurrentPhrase(newPhrase)
        Option(newPhrase)
      }
    }.tap { phrase =>
      if ( ! currentSongOpt.contains(phrase.song)) {
        setCurrentSong(phrase.song)
      }
      setCurrentPhrase(phrase)
    }
  }

  private def selectLastPhraseFromPreviousSong(): Option[Phrase] = {
    selectPreviousSong().flatMap { song => Option(song.phrases.last) }
  }

  private def selectFirstPhraseInNextSong(): Option[Phrase] = {
    selectNextSong().flatMap { song => Option(song.phrases.head) }
  }

  private def indexOfCurrentSong() = {
    withCurrentSetListAndSong { (setList, song) => setList.songs.indexOf(song) }
  }

  private def indexOfCurrentPhrase() = {
    withCurrentSongAndPhrase { (song, phrase) => song.phrases.indexOf(phrase) }
  }

  private def withCurrentSongAndPhrase[T](f: (Song, Phrase) => T) = {
    currentSongOpt.flatMap { song =>
      currentPhraseOpt.flatMap { phrase => Option(f(song, phrase)) }
    }
  }

  private def withCurrentSetListAndSong[T](f: (SetList, Song) => T) = {
    setListOpt.flatMap { setList =>
      currentSongOpt.flatMap { song => Option(f(setList, song)) }
    }
  }

  def selectPreviousSong(): Option[Song] = {
    indexOfCurrentSong().flatMap { index => selectSongAt(index - 1) }
  }

  def selectNextSong() = {
    indexOfCurrentSong().flatMap { index => selectSongAt(index + 1) }
  }

  private def selectSongAt(index: Int): Option[Song] = {
    withCurrentSetListAndSong { (setList, song) =>
      val newCurrentSong = setList.songs(wrapIndex(index, setList.songs))
      setCurrentSong(newCurrentSong)
      newCurrentSong
    }
  }

  def trigger(trigger: TriggerOnMessage): Unit = {
    withCurrentPhraseInSong { (phrase, song) =>
      val instrument = phrase.instrumentFor(trigger.name, trigger.handleValue)
      val velocity = instrument.mapValueToVelocity(trigger.triggerValue)
      instrument.notes.foreach { (note) =>
        midiIO.sendNoteOn(song.channel, MidiNoteMap.valueForNote(note), velocity)
      }
      triggeredState.triggered(trigger.name, instrument, song.channel)
      instrument.fireTriggerOnListener(velocity)
    }
  }

  def off(name: String): Unit = {
    triggeredState.lastTriggered(name) match {
      case Some(Tuple2(instrument, channel)) =>
        instrument.notes.foreach { (note) =>
          midiIO.sendNoteOff(channel, MidiNoteMap.valueForNote(note), 0)
        }
        instrument.fireTriggerOffListener()
      case None =>
        System.err.println(f"Somehow managed to trigger an off event with no previous on event for $name. Ignoring.")
    }
  }

  private def withCurrentPhraseInSong(f: (Phrase, Song) => Unit): Unit = {
    for {
      song <- currentSongOpt
      phrase <- currentPhraseOpt
    } yield {
      f(phrase, song)
    }
  }


  def newSetList = {
    val setList = SetList(name = "New Set List")
    setList.addSong()

    this.setListOpt = Option(setList)

    setList
  }
}

/**
  * TODO: rotating the handle selects the instrument. To change the number of instruments done by providing a different
  * array of instruments.
  *
  * So what does changing the threshold values do? Allow for a wider range of rotation motion.
  * Also, to change from a linear mapping of values to a logarithmic mapping requires a different implementation of
  * TriggerMap with a variety of arguments that might specify a bezier curve or something more radical. In my case,
  * random will likely be something I decide I want.
  */
object Thresholds {
  val lowHandleThreshold = 100
  val highHandleThreshold = 800
}

case class SetList(var name: String) {

  @JsonManagedReference var songs: Array[Song] = Array.empty

  def addSong() = {
    val newPhrase = Phrase("Phrase 1", 1)
    newPhrase.addNewKickInstrument()
    newPhrase.addNewSnareInstrument()

    val channel = next(songs, (s: Song) => { s.channel })

    // TODO: change song name, can't dup names.
    val newSong = Song(s"Song $channel", channel)
    newSong.phrases = Array[Phrase](newPhrase)
    newSong.setList = this

    songs = songs :+ newSong
    newSong
  }

  def updateSongs(songs: Seq[Song]): Unit = {
    this.songs = songs.toArray
  }

  def write(): Unit = {
    SetListSerializer.write(this)
  }

  def setName(name: String): Unit = {
    this.name = name
  }
}

case class Song(var name: String,
                var channel: Int
               ) extends Selectable {

  @JsonManagedReference var phrases: Array[Phrase] = Array.empty
  @JsonBackReference var setList: SetList = null

  def addPhrase() = {
    val nextPatch = next(phrases, (p: Phrase) => {p.patch })

    val newPhrase = Phrase(s"Phrase $nextPatch", nextPatch)
    newPhrase.kickInstruments = Array(Instrument(Array("C0")))
    newPhrase.snareInstruments = Array(Instrument(Array("C0")))
    newPhrase.song = this

    phrases = phrases :+ newPhrase

    newPhrase
  }

  def updatePhrases(phrases: Seq[Phrase]) = {
    this.phrases = phrases.toArray
  }

  def setName(name: String): Unit = {
    this.name = name
  }

  def setChannel(channel: Int): Unit = {
    this.channel = channel
  }
}

case class Phrase(var name: String, var patch: Int) extends Selectable {

  @JsonManagedReference var kickInstruments: Array[Instrument] = Array.empty
  @JsonManagedReference var snareInstruments: Array[Instrument] = Array.empty
  @JsonBackReference var song: Song = null

  def addNewKickInstrument(): Unit = {
    kickInstruments = kickInstruments :+ newInstrument
  }

  def addNewSnareInstrument(): Unit = {
    snareInstruments = snareInstruments :+ newInstrument
  }

  def instrumentFor(name: String, handleValue: Int): Instrument = {
    name match {
      case "KICK" => instrumentsByValue(kickInstruments, handleValue)
      case "SNARE" => instrumentsByValue(snareInstruments, handleValue)
      case _ => throw new IllegalArgumentException(f"unknown trigger $name")
    }
  }

  private def newInstrument = {
    // TODO: default midi note 0? Shrug. maybe next one after the last instrument?
    val newInstrument = new Instrument(Array("C0"))
    newInstrument.phrase = this
    newInstrument
  }

  private def instrumentsByValue(instruments: Array[Instrument], value: Int) = {
    import Thresholds._ // TODO: config this, eh?
    val idx = map(value, lowHandleThreshold, highHandleThreshold, 0, instruments.length - 1)
    instruments(idx.toInt)
  }
}


case class Instrument(var notes: Array[String]) {

  @JsonBackReference var phrase: Phrase = null

  private var triggeredOnListener: Option[(Int) => Unit] = None
  private var triggeredOffListener: Option[() => Unit] = None
  private var threshold = 900

  def setThreshold(threshold: Int = 900) = {
    this.threshold = threshold
    this
  }

  def mapValueToVelocity(value: Int) = {
    constrain(map(value, 0, threshold, 0, 127), 0, 127).toInt
  }

  def setNotes(notes: Array[String]) {
    this.notes = notes
  }

  def wasTriggeredOn(f: (Int) => Unit): Unit = {
    this.triggeredOnListener = Option(f)
  }

  def wasTriggeredOff(f: () => Unit): Unit = {
    this.triggeredOffListener = Option(f)
  }

  def fireTriggerOnListener(pitch: Int):Unit = {
    triggeredOnListener.foreach { listener => listener(pitch) }
  }

  def fireTriggerOffListener():Unit = {
    triggeredOffListener.foreach { listener => listener() }
  }
}


