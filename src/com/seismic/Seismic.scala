package com.seismic

import java.io.File

import com.fasterxml.jackson.annotation.{JsonBackReference, JsonManagedReference}
import com.seismic.io.{Preferences, SetListSerializer}
import com.seismic.io.Preferences.getPreferences
import com.seismic.messages.TriggerOnMessage
import com.seismic.midi.{MIDIIO, MidiNoteMap}
import com.seismic.ui.swing.Selectable
import com.seismic.utils.ValueMapHelper.map
import com.seismic.utils.ArrayUtils.wrapIndex
import com.seismic.scala.OptionExtensions._
import com.seismic.utils.Next.{highest, next}
import com.seismic.scala.ArrayExtensions._

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

  var onPhraseChangeHandlerOpt: Option[(Phrase) => Unit] = None

  def trigger(trigger: TriggerOnMessage): Unit = {
    withCurrentPhraseInSong { (phrase, song) =>
      val instrument = phrase.instrumentFor(trigger.name, trigger.handleValue)
      val velocity = instrument.mapValueToVelocity(trigger.triggerValue)
      instrument.notes.foreach { (note) =>
        if (note.startsWith("N")) {
          midiIO.sendNoteOff(song.channel, MidiNoteMap.valueForNote(note.drop(1)), 0)
        } else if (note.startsWith("X")) {
          midiIO.sendNoteOn(song.channel, MidiNoteMap.valueForNote(note.drop(1)), velocity)
        } else if ( ! note.startsWith("T")) {
          midiIO.sendNoteOn(song.channel, MidiNoteMap.valueForNote(note), velocity)
        }
      }
      triggeredState.triggered(trigger.name, instrument, song.channel)
      instrument.fireTriggerOnListener(velocity)
    }
  }

  def off(name: String): Unit = {
    triggeredState.lastTriggered(name) match {
      case Some(Tuple2(instrument, channel)) =>
        instrument.notes.foreach { (note) =>
          if (note.startsWith("T")) {
            midiIO.sendNoteOff(channel, MidiNoteMap.valueForNote(note.drop(1)), 0)
          } else if (!note.startsWith("X") && !note.startsWith("N")) {
            midiIO.sendNoteOff(channel, MidiNoteMap.valueForNote(note), 0)
          }
        }
        instrument.fireTriggerOffListener()
      case None =>
        System.err.println(f"Somehow managed to trigger an off event with no previous on event for $name. Ignoring.")
    }
  }

  def patch(patch: Int): Unit = {
    println(f"patch switch: $patch")
    currentSongOpt.foreach { song =>
      song.phrases.find { phrase =>
        phrase.patch == patch
      } match {
        case Some(phrase) => setCurrentPhrase(phrase)
        case None => System.out.println(s"No phrase in $currentSongOpt with patch $patch")
      }
    }
  }

  def selectNextPhrase(): Unit = {
    indexOfCurrentPhrase().flatMap { index => selectPhraseAt(index + 1) }
  }

  def selectPreviousPhrase(): Unit = {
    indexOfCurrentPhrase().flatMap { index => selectPhraseAt(index - 1) }
  }

  def newSetList = {
    val setList = SetList(name = "New Set List")
    setList.addSong()

    this.setListOpt = Option(setList)
    this.setCurrentSong(setList.songs.head)

    setList
  }

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
    setCurrentPhrase(song.phrases.head)
  }

  def setCurrentPhrase(phrase: Phrase): Unit = {
    currentPhraseOpt = Option(phrase)
    onPhraseChangeHandlerOpt.foreach { handler => handler(phrase) }
  }

  def onPhraseChange(handler: (Phrase) => Unit): Unit = {
    onPhraseChangeHandlerOpt = Option(handler)
  }

  private def getPreviousSong = {
    indexOfCurrentSong().flatMap { index => getSongAt(index - 1) }
  }

  private def getNextSong = {
    indexOfCurrentSong().flatMap { index => getSongAt(index + 1) }
  }

  private def selectPhraseAt(index: Int): Option[Phrase] = {
    currentSongOpt.flatMap { song =>
      if (song.phrases.length == index) {
        getFirstPhraseInNextSong
      } else if (index < 0) {
        getLastPhraseFromPreviousSong
      } else {
        Option(song.phrases(wrapIndex(index, song.phrases)))
      }
    }.tap { phrase =>
      currentSongOpt = Option(phrase.song)
      setCurrentPhrase(phrase)
    }
  }

  private def getLastPhraseFromPreviousSong: Option[Phrase] = {
    getPreviousSong.flatMap { song => Option(song.phrases.last) }
  }

  private def getFirstPhraseInNextSong: Option[Phrase] = {
    getNextSong.flatMap { song => Option(song.phrases.head) }
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

  private def getSongAt(index: Int): Option[Song] = {
    withCurrentSetListAndSong { (setList, song) =>
      setList.songs(wrapIndex(index, setList.songs))
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
}

case class SetList(var name: String) {

  @JsonManagedReference var songs: Array[Song] = Array.empty

  def addSong() = {

    val channel = next(songs, (s: Song) => { s.channel })

    // TODO: change song name, can't dup names.
    val newSong = Song(s"Song $channel", channel)
    newSong.addPhrase()
    newSong.setList = this

    songs = songs :+ newSong
    newSong
  }

  def removeSong(song: Song): Unit = {
    songs = songs.remove(song)
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
    newPhrase.song = this
    newPhrase.addNewKickInstrument()
    newPhrase.addNewSnareInstrument()

    phrases = phrases :+ newPhrase

    newPhrase
  }

  def updatePhrases(phrases: Seq[Phrase]) = {
    this.phrases = phrases.toArray
  }

  def removePhrase(phrase: Phrase): Unit = {
    phrases = phrases.remove(phrase)
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

  def removeKickInstrument(instrument: Instrument): Unit = {
    kickInstruments = kickInstruments.remove(instrument)
  }

  def removeSnareInstrument(instrument: Instrument): Unit = {
    snareInstruments = snareInstruments.remove(instrument)
  }

  def instrumentFor(name: String, handleValue: Int): Instrument = {
    name match {
      case "KICK" => selectInstrumentForValue(kickInstruments, handleValue)
      case "SNARE" => selectInstrumentForValue(snareInstruments, handleValue)
      case _ => throw new IllegalArgumentException(f"unknown trigger $name")
    }
  }

  private def nextNote() = {
    MidiNoteMap.noteForValue(nextNoteForInstruments( allInstruments() ))
  }

  private def allInstruments(): Seq[Instrument] = {
    song.phrases.flatMap { phrase => phrase.kickInstruments ++ phrase.snareInstruments } ++ kickInstruments ++ snareInstruments
  }

  private def nextNoteForInstruments(instruments: Seq[Instrument]) = {
    next(instruments, (instrument: Instrument) => instrument.highestNote(), -1)
  }

  private def newInstrument = {
    val note = nextNote()
    val newInstrument = new Instrument(Array(note))
    newInstrument.phrase = this
    newInstrument
  }

  private def selectInstrumentForValue(instruments: Array[Instrument], value: Int) = {
    getPreferences.handleCalibration.select(value, instruments)
  }
}


/**
  * TODO: Toggle ability to send midi note off from a trigger on message  instead of the original trigger off message.
  *
  * @param notes
  */
case class Instrument(var notes: Array[String]) {

  @JsonBackReference var phrase: Phrase = null

  private var triggeredOnListener: Option[(Int) => Unit] = None
  private var triggeredOffListener: Option[() => Unit] = None
  private var threshold = 900

  def setThreshold(threshold: Int = 900) = {
    this.threshold = threshold
    this
  }

  def mapValueToVelocity(value: Int): Int = {
    val mappedValue = map(value, 0, threshold, 0, 127)
    Math.max(Math.min(mappedValue, 127), 0).toInt
  }

  def setNotes(notes: Array[String]) {
    this.notes = notes
  }

  def highestNote() = {
    highest(notes, (note: String) => MidiNoteMap.valueForNote(note))
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


