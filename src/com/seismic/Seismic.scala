package com.seismic

import com.seismic.messages.TriggerOnMessage
import com.seismic.midi.MIDIIO

class Seismic(midiIO: MIDIIO) {

  val builder = new MIDIInstrumentBuilder(midiIO)

  // TODO: if I want to change instruments, I have to change these arrays, which means changing the TrigerMap which means changing et cetc etc
  //
  // So, if I want song A to be 3 instruments, Song B to be 2, and Song C to be 7, I have to create new maps each time.
  // So, maybe I should adjust the grouping of what is changeable to things that change via knob and things that
  // change via song change (foot controller? Or maybe not!)
  //
  // A song change might be a button or select in the UI: which midi banks/channels/mappings do I use for this song?
  val kickInstruments = Array(
    builder.instrument(0),
    builder.instrument(1),
    builder.instrument(2)
  )

  val snareInstruments = Array(
    builder.instrument(3),
    builder.instrument(4),
    builder.instrument(5)
  )

  // TODO: I want to change thresholds or change midi channels on the fly depending on the song, or which channels are mapped
  val kickMap = new TriggerMap(100, 800, kickInstruments)
  val snareMap = new TriggerMap(100, 800, snareInstruments)

  val triggeredState = new TriggeredState

  def midiMap(name: String) = name match {
    case "KICK" => kickMap
    case "SNARE" => snareMap
  }

  def trigger(trigger: TriggerOnMessage): Unit = {
    val instrument = midiMap(trigger.name).mapValue(trigger.handleValue)
    instrument.noteOn(trigger.triggerValue)
    triggeredState.triggered(trigger.name, instrument)
  }

  def off(name: String): Unit = {
    triggeredState.lastTriggered(name) match {
      case Some(instrument) => instrument.noteOff
      case None => System.err.println(
        "Somehow managed to trigger an off event with no previous on event for " + name + ". Ignoring.")
    }
  }
}
