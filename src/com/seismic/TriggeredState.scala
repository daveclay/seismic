package com.seismic

class TriggeredState {
  var lastTriggeredMap = Map[String, MIDIInstrument]()

  def triggered(name: String, instrument: MIDIInstrument): Unit = {
    lastTriggeredMap = lastTriggeredMap + (name -> instrument)
  }

  def lastTriggered(name: String) = {
    lastTriggeredMap.get(name)
  }
}
