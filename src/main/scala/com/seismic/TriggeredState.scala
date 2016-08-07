package com.seismic

class TriggeredState {

  var lastTriggeredMap = Map[String, (Instrument, Song)]()

  def triggered(name: String, instrument: Instrument, song: Song): Unit = {
    lastTriggeredMap = lastTriggeredMap + (name -> (instrument, song))
  }

  def lastTriggered(name: String) = {
    lastTriggeredMap.get(name)
  }
}
