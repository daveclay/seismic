package com.seismic

import java.io.File

import collection.mutable.ArrayBuffer
import com.seismic.messages.TriggerOnMessage
import com.seismic.midi.MIDIIO
import com.seismic.utils.SetListSerializer
import com.seismic.utils.ValueMapHelper.map
import processing.core.PApplet.constrain

/**
  * Contains the structure and management of a SetList of Songs and MIDIInstruments
  * TODO: should I rename "phrase" to "scene" to match Maschine?
  * @param midiIO
  */
class Seismic(midiIO: MIDIIO) {

  var setListOpt: Option[SetList] = None

  def openSetList(file: File) = {
    val setList = SetListSerializer.read(file)
    setListOpt = Option(setList)
    setList
  }

  val triggeredState = new TriggeredState

  def trigger(trigger: TriggerOnMessage): Unit = {
    setListOpt.foreach { setList =>
      val song = setList.songs(0)
      val phrase = song.phrases(0)

      val instrument = phrase.instrumentFor(trigger.name, trigger.handleValue)
      val pitch = instrument.mapValueToVelocity(trigger.triggerValue)
      instrument.notes.foreach { (note) =>
        midiIO.sendNoteOn(song.channel, note, pitch)
      }

      triggeredState.triggered(trigger.name, instrument)

    }
  }

  def off(name: String): Unit = {
    setListOpt.foreach { setList =>
      val song = setList.songs(0)
      triggeredState.lastTriggered(name) match {
        case Some(instrument) =>
          instrument.notes.foreach { (note) =>
            midiIO.sendNoteOff(song.channel, note, 0)
          }
        case None =>
          System.err.println(f"Somehow managed to trigger an off event with no previous on event for $name. Ignoring.")
      }
    }
  }

  def getEmptySetList = {
    SetList(
             name = "New Set List",
             songs = ArrayBuffer(
                            Song(
                                  name = "Song A",
                                  channel = 1,
                                  phrases = ArrayBuffer(
                                                   Phrase(
                                                           name = "Intro",
                                                           kickInstruments = ArrayBuffer(
                                                                                    Instrument(ArrayBuffer(60)),
                                                                                    Instrument(ArrayBuffer(61)),
                                                                                    Instrument(ArrayBuffer(62))
                                                                                  ),
                                                           snareInstruments = ArrayBuffer(
                                                                                     Instrument(ArrayBuffer(63))
                                                                                   )
                                                         )
                                                 )
                                )
                          )
           )
  }
}

case class Instrument(var notes: ArrayBuffer[Int]) {

  private var threshold = 900

  def setThreshold(threshold: Int = 900) = {
    this.threshold = threshold
    this
  }

  def mapValueToVelocity(value: Int) = {
    constrain(map(value, 0, threshold, 0, 127), 0, 127).toInt
  }

  def setNotes(notes: ArrayBuffer[Int]) {
    this.notes = notes
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

case class SetList(var name: String, var songs: ArrayBuffer[Song]) {

  def write(): Unit = {
    SetListSerializer.write(this)
  }

  def setName(name: String): Unit = {
    this.name = name
  }
}

case class Song(var name: String,
                var channel: Int,
                phrases: ArrayBuffer[Phrase]) {

  def addPhrase() = {
    val newPhrase = Phrase("New Phrase",
                            ArrayBuffer(Instrument(ArrayBuffer(0))),
                            ArrayBuffer(Instrument(ArrayBuffer(0))))
    phrases += newPhrase

    newPhrase
  }

  def setName(name: String): Unit = {
    this.name = name
  }

  def setChannel(channel: Int): Unit = {
    this.channel = channel
  }
}

case class Phrase(var name: String,
                  kickInstruments: ArrayBuffer[Instrument],
                  snareInstruments: ArrayBuffer[Instrument]) {

  def addNewKickInstrument(): Unit = {
    // TODO: default midi note 0? Shrug. maybe next one after the last instrument?
    kickInstruments += new Instrument(ArrayBuffer(0))
  }

  def addNewSnareInstrument(): Unit = {
    // TODO: default midi note 0? Shrug. maybe next one after the last instrument?
    snareInstruments += new Instrument(ArrayBuffer(0))
  }

  def addSnareInstruments(instrument: Instrument): Unit = {
    snareInstruments += instrument
  }

  private def instrumentsByValue(instruments: ArrayBuffer[Instrument]) = {
    import Thresholds._ // TODO: config this, eh?
    (value: Int) => {
      val idx = map(value, lowHandleThreshold, highHandleThreshold, 0, instruments.length - 1)
      instruments(idx.toInt)
    }
  }

  private val instrumentMapByName = Map(
                                         "KICK" -> instrumentsByValue(kickInstruments),
                                         "SNARE" -> instrumentsByValue(snareInstruments)
                                       )

  def instrumentFor(name: String, handleValue: Int) = {
    instrumentMapByName(name)(handleValue)
  }

  def setName(name: String) = {
    this.name = name
  }
}


