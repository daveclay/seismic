package com.seismic.midi

class MidiThread(midiIO: MIDIIO) extends Thread {
  // todo: pub sub? the serial thread will post a message, which gets interpreted, then passed to
  // this thread for sending off on the midi thread. the main animation thread can sit idle then mostly.
}
