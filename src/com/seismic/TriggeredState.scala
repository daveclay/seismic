package com.seismic

class TriggeredState {

  var lastTriggeredMap = Map[String, (Instrument, Int)]()

  def triggered(name: String, instrument: Instrument, channel: Int): Unit = {
    lastTriggeredMap = lastTriggeredMap + (name -> (instrument, channel))
  }

  def lastTriggered(name: String) = {
    lastTriggeredMap.get(name)
  }
}
