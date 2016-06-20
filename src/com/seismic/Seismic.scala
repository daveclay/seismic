package com.seismic

import java.io.File

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.seismic.messages.TriggerOnMessage
import com.seismic.midi.MIDIIO
import com.seismic.utils.ValueMapHelper.map
import processing.core.PApplet.constrain

/**
  * Contains the structure and management of a SetList of Songs and MIDIInstruments
  **
  *val sampleSetList = SetList(
  *name = "The Setlist",
  *songs = Array(
  *Song(
  *name = "Song A",
  *channel = 1,
  *phrases = Array(
  *Phrase(
  *name = "intro",
  *kickInstruments = Array(
  *Instrument(Array(60)),
  *Instrument(Array(61)),
  *Instrument(Array(62))
  *),
  **
  *snareInstruments = Array(
  *Instrument(Array(63)),
  *Instrument(Array(64)),
  *Instrument(Array(65, 66)) // You can send two midi notes at the same time. Kick and snare, for instance.
  *)
  *)
  *)
  *)
  *)
  *)
  *
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
}

case class Instrument(var notes: Array[Int]) {

  private var threshold = 900

  def setThreshold(threshold: Int = 900) = {
    this.threshold = threshold
    this
  }

  def mapValueToVelocity(value: Int) = {
    constrain(map(value, 0, threshold, 0, 127), 0, 127).toInt
  }

  def setNotes(notes: Array[Int]) {
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

object SetListSerializer {

  def write(setList: SetList): Unit = {
    objectMapper().writeValue(new File(f"${setList.name}.json"), setList)
  }

  def read(file: File) = {
    objectMapper().readValue(file, classOf[SetList])
  }

  private def objectMapper() = {
    val mapper = new ObjectMapper
    mapper.registerModule(DefaultScalaModule)
  }

}

case class SetList(var name: String, var songs: Array[Song]) {

  def write(): Unit = {
    SetListSerializer.write(this)
  }
}

case class Song(var name: String,
                var channel: Int,
                phrases: Array[Phrase]) {

  def setName(name: String): Unit = {
    this.name = name
  }

  def setChannel(channel: Int): Unit = {
    this.channel = channel
  }
}

case class Phrase(var name: String,
                  kickInstruments: Array[Instrument],
                  snareInstruments: Array[Instrument]) {

  import Thresholds._

  private def instrumentsByValue(instruments: Array[Instrument]) = {
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


