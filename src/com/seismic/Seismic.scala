package com.seismic

import com.seismic.messages.TriggerOnMessage
import com.seismic.midi.MIDIIO

/**
  * Contains the structure and management of a SetList of Songs and MIDIInstruments
  * @param midiIO
  */
class Seismic(midiIO: MIDIIO) {

  // TODO: edit this in the Processing UI, export/import JSON config.
  val setList = SetList(
    name = "The Setlist",
    songs = Array(
      Song(
        name = "Song A",
        channel = 1,
        phrases = Array(
          Phrase(
            name = "intro",
            kickInstruments = Array(
              Instrument(60),
              Instrument(61),
              Instrument(62)
            ),

            snareInstruments = Array(
              Instrument(63),
              Instrument(64),
              Instrument(65, 66) // You can send two midi notes at the same time. Kick and snare, for instance.
            )
          )
        )
      )
    )
  )

  // TODO: select song/phrase via footswitch via message sent over serial
  val song = setList.songs(0)
  val phrase = song.phrases(0)

  val triggeredState = new TriggeredState

  def trigger(trigger: TriggerOnMessage): Unit = {
    val instrument = phrase.instrumentFor(trigger.name, trigger.handleValue)
    val pitch = instrument.mapValueToVelocity(trigger.triggerValue)
    instrument.notes.foreach { (note) =>
      midiIO.sendNoteOn(song.channel, note, pitch)
    }

    triggeredState.triggered(trigger.name, instrument)
  }

  def off(name: String): Unit = {
    triggeredState.lastTriggered(name) match {
      case Some(instrument) =>
        instrument.notes.foreach { (note) =>
          midiIO.sendNoteOff(song.channel, note, 0)
        }
      case None => System.err.println(
        "Somehow managed to trigger an off event with no previous on event for " + name + ". Ignoring.")
    }
  }
}
