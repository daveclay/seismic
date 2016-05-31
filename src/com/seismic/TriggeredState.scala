package com.seismic

class TriggeredState {
  var lastTriggeredMap = Map[String, Instrument]()

  def triggered(name: String, instrument: Instrument): Unit = {
    lastTriggeredMap = lastTriggeredMap + (name -> instrument)
  }

  def lastTriggered(name: String) = {
    lastTriggeredMap.get(name)
  }
}
