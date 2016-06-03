package com.seismic

import processing.core.PApplet.{constrain, map}

case class Instrument(notes: Int*) {

  private var threshold = 900

  def setThreshold(threshold: Int = 900) = {
    this.threshold = threshold
    this
  }

  def mapValueToVelocity(value: Int) = {
    constrain(map(value, 0, threshold, 0, 127), 0, 127).toInt
  }
}


