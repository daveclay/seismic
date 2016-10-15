package com.seismic

import java.io.File

import com.fasterxml.jackson.annotation.{JsonBackReference, JsonManagedReference}
import com.seismic.io.{Preferences, SetListSerializer}
import com.seismic.messages.TriggerOnMessage
import com.seismic.midi.{MIDIIO, MidiNoteMap}
import com.seismic.ui.utils.Selectable
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
class Seismic(midiIO: MIDIIO, preferences: Preferences, triggeredState: TriggeredState) {

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
          sendNoteOff(song, note)
        } else if (note.startsWith("X")) {
          sendNoteOn(song, note, velocity)
        } else if ( ! note.startsWith("T")) {
          sendNoteOn(song, note, velocity)
        }
      }
      triggeredState.triggered(trigger.name, instrument, song)
      instrument.fireTriggerOnListener(velocity)
    }
  }

  def off(name: String): Unit = {
    triggeredState.lastTriggered(name) match {
      case Some(Tuple2(instrument, song)) =>
        instrument.notes.foreach { (note) =>
          if (note.startsWith("T")) {
            sendNoteOff(song, note)
          } else if (!note.startsWith("X") && !note.startsWith("N")) {
            sendNoteOff(song, note)
          }
        }
        instrument.fireTriggerOffListener()
      case None =>
        System.err.println(f"Somehow managed to trigger an off event with no previous on event for $name. Ignoring.")
    }
  }

  private def sendNoteOn(song: Song, note: String, velocity: Int = 0): Unit = {
    midiIO.sendNoteOn(MidiNoteMap.channelForNote(note, song.channel - 1),
                       MidiNoteMap.midiValueForNote(note),
                       velocity)
  }

  private def sendNoteOff(song: Song, note: String): Unit = {
    midiIO.sendNoteOff(MidiNoteMap.channelForNote(note, song.channel - 1),
                        MidiNoteMap.midiValueForNote(note),
                        0)
  }

  def patch(patch: Int): Unit = {
    println(f"patch switch: $patch")
    currentSongOpt.foreach { song =>
      song.getPhrases.find { phrase =>
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

  def setSetList(setList: SetList): Unit = {
    setListOpt = Option(setList)
    setList.setPreferences(preferences)
    setCurrentSong(setList.songs.head)
    setCurrentPhrase(setList.songs.head.getPhrases.head)
  }

  def openSetList(file: File) = {
    val setList = SetListSerializer.read(file)
    setSetList(setList)
    setList
  }

  def save(): Unit = {
    setListOpt.foreach { setlist => setlist.write() }
  }

  def setCurrentSong(song: Song): Unit = {
    currentSongOpt = Option(song)
    song.setPreferences(preferences)
    setCurrentPhrase(song.getPhrases.head)
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
      if (song.getPhrases.length == index) {
        getFirstPhraseInNextSong
      } else if (index < 0) {
        getLastPhraseFromPreviousSong
      } else {
        Option(song.getPhrases(wrapIndex(index, song.getPhrases)))
      }
    }.tap { phrase =>
      currentSongOpt = Option(phrase.song)
      setCurrentPhrase(phrase)
    }
  }

  private def getLastPhraseFromPreviousSong: Option[Phrase] = {
    getPreviousSong.flatMap { song => Option(song.getPhrases.last) }
  }

  private def getFirstPhraseInNextSong: Option[Phrase] = {
    getNextSong.flatMap { song => Option(song.getPhrases.head) }
  }

  private def indexOfCurrentSong() = {
    withCurrentSetListAndSong { (setList, song) => setList.songs.indexOf(song) }
  }

  private def indexOfCurrentPhrase() = {
    withCurrentSongAndPhrase { (song, phrase) => song.getPhrases.indexOf(phrase) }
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
  private var preferencesOpt: Option[Preferences] = None

  def setPreferences(preferences: Preferences) = {
    this.preferencesOpt = Option(preferences)
    songs.foreach { song => song.setPreferences(preferences) }
  }

  def addSong() = {
    preferencesOpt match {
      case Some(preferences) =>
        val channel = next(songs, (s: Song) => { s.channel })
        // TODO: change song name, can't dup names.
        val newSong = Song(s"Song $channel", channel)
        newSong.setPreferences(preferences)
        newSong.addPhrase()
        newSong.setList = this

        songs = songs :+ newSong
        newSong

      case None => throw new IllegalStateException("Can't add song because somehow preferences weren't set.")
    }
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
                var channel: Int) extends Selectable {

  private var phrases: Array[Phrase] = Array.empty
  @JsonBackReference var setList: SetList = null
  private var preferencesOpt: Option[Preferences] = None

  def setPreferences(preferences: Preferences) = {
    this.preferencesOpt = Option(preferences)
    phrases.foreach { phrase => setPrefsOnPhrase(phrase, preferences) }
  }
  
  private def setPrefsOnPhrase(phrase: Phrase, preferences: Preferences): Unit = {
    phrase.setTriggerThresholds(preferences.triggerThresholds)
    phrase.setHandleCalibration(preferences.handleCalibration)
  }

  @JsonManagedReference
  def setPhrases(phrases: Array[Phrase]): Unit = {
    phrases.foreach { phrase => phrase.song = this }
    this.phrases = phrases
  }

  def getPhrases = phrases

  def dupPhrase(phrase: Phrase) = {
    withNewPhrase { newPhrase =>
      newPhrase.setKickInstruments(phrase.getKickInstruments.map { instrument => Instrument(instrument.notes.clone()) })
      newPhrase.setSnareInstruments(phrase.getSnareInstruments.map { instrument => Instrument(instrument.notes.clone()) })
    }
  }

  def addPhrase() = {
    withNewPhrase { newPhrase =>
      newPhrase.addNewKickInstrument()
      newPhrase.addNewSnareInstrument()
    }
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

  private def withNewPhrase[T](f: (Phrase) => T) = {
    val newPhrase = createPhrase()
    withPrefs { preferences =>
      setPrefsOnPhrase(newPhrase, preferences)
      f(newPhrase)
      phrases = phrases :+ newPhrase
      newPhrase
    }
  }

  private def withPrefs[T](f: (Preferences) => T) = {
    preferencesOpt match {
      case Some(preferences) =>
        f(preferences)
      case None => throw new IllegalStateException("Preferences weren't set!")
    }
  }

  private def createPhrase() = {
    val patch = nextPatch()
    val newPhrase = Phrase(s"Phrase $patch", patch)
    newPhrase.song = this
    newPhrase
  }

  private def nextPatch() = {
    next(phrases, (p: Phrase) => {p.patch })
  }
}

case class Phrase(var name: String, var patch: Int) extends Selectable {
  private var kickInstruments: Array[Instrument] = Array.empty
  private var snareInstruments: Array[Instrument] = Array.empty
  @JsonBackReference var song: Song = null

  var triggerThresholdsOpt: Option[TriggerThresholds] = None
  var handleCalibrationOpt: Option[HandleCalibration] = None

  def setHandleCalibration(handleCalibration: HandleCalibration): Unit = {
    handleCalibrationOpt = Option(handleCalibration)
  }

  def setTriggerThresholds(triggerThresholds: TriggerThresholds): Unit = {
    triggerThresholdsOpt = Option(triggerThresholds)
    kickInstruments.foreach { instrument => instrument.setTriggerThreshold(() => triggerThresholds.kickThreshold) }
    snareInstruments.foreach { instrument => instrument.setTriggerThreshold(() => triggerThresholds.snareThreshold) }
  }

  def getKickInstruments = kickInstruments

  @JsonManagedReference
  def setKickInstruments(kickInstruments: Array[Instrument]): Unit = {
    setPhraseOnInstruments(kickInstruments)
    this.kickInstruments = kickInstruments
  }

  def dupKickInstruments() = {
    dupInstruments(kickInstruments)
  }

  def getSnareInstruments = snareInstruments

  @JsonManagedReference
  def setSnareInstruments(snareInstruments: Array[Instrument]): Unit = {
    setPhraseOnInstruments(snareInstruments)
    this.snareInstruments = snareInstruments
  }

  def dupSnareInstruments() = {
    dupInstruments(snareInstruments)
  }

  private def dupInstruments(instruments: Seq[Instrument]) = {
    instruments.map { instrument => Instrument(instrument.notes.clone()) }
  }

  def addNewKickInstrument(): Instrument = {
    withTriggerThresholds(triggerThresholds => {
      val instrument = newInstrument(() => triggerThresholds.kickThreshold)
      kickInstruments = kickInstruments :+ instrument
      instrument
    })
  }

  def addNewSnareInstrument(): Instrument = {
    withTriggerThresholds(triggerThresholds => {
      val instrument = newInstrument(() => triggerThresholds.snareThreshold)
      snareInstruments = snareInstruments :+ instrument
      instrument
    })
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

  private def setPhraseOnInstruments(instruments: Seq[Instrument]): Unit = {
    instruments.foreach { instrument => instrument.phrase = this }
  }

  private def withTriggerThresholds[T](f: (TriggerThresholds) => T) = {
    triggerThresholdsOpt match {
      case Some(triggerThresholds) => f(triggerThresholds)
      case None => throw new IllegalStateException("Somehow I have no triggerThresholds yet trying to play the instrument")
    }
  }

  private def nextNote() = {
    MidiNoteMap.noteForMidiValue(nextNoteForInstruments(kickInstruments ++ snareInstruments))
  }

  private def nextNoteForInstruments(instruments: Seq[Instrument]) = {
    next(instruments, (instrument: Instrument) => instrument.highestNote(), -1, 127)
  }

  private def newInstrument(triggerThreshold: () => Int) = {
    val note = nextNote()
    val newInstrument = new Instrument(Array(note))
    newInstrument.setTriggerThreshold(triggerThreshold)
    newInstrument.phrase = this
    newInstrument
  }

  private def selectInstrumentForValue(instruments: Array[Instrument], value: Int) = {
    handleCalibrationOpt match {
      case Some(handleCalibration) => handleCalibration.select(value, instruments)
      case None => throw new IllegalStateException("Somehow I have no HandleCalibration yet someone's trying to play the instrument")
    }
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
  private var triggerThreshold: Option[() => Int] = None

  def setTriggerThreshold(triggerThreshold: () => Int): Unit = {
    this.triggerThreshold = Option(triggerThreshold)
  }

  def mapValueToVelocity(value: Int): Int = {
    val threshold = triggerThreshold match {
      case Some(f) => f()
      case None => 900
    }
    val mappedValue = map(value, 0, threshold, 0, 127)
    Math.max(Math.min(mappedValue, 127), 0).toInt
  }

  def setNotes(notes: Array[String]) {
    this.notes = notes
  }

  def highestNote() = {
    highest(notes, (note: String) => MidiNoteMap.midiValueForNote(note))
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


