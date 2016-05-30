package com.seismic

import com.seismic.midi.MIDIIO

class MIDIInstrument(midiIO: MIDIIO,
                     channel: Int,
                     config: InstrumentTriggerConfig) {

  def noteOn(value: Int): Unit = {
    midiIO.sendNoteOn(channel, config.note, config.mapValueToVelocity(value));
  }

  def noteOff() {
    midiIO.sendNoteOff(channel, config.note, 0);
  }
}

class MIDIInstrumentBuilder(midiIO: MIDIIO) {

  def instrument(channel: Int,
                 config: InstrumentTriggerConfig = new InstrumentTriggerConfig) = {
    new MIDIInstrument(midiIO, channel, config)
  }
}


