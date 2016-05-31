package com.seismic

import processing.core.PApplet.{constrain, map}

case class Instrument(note: Int,
                      threshold: Int = 900) {

  def mapValueToVelocity(value: Int) = {
    constrain(map(value, 0, threshold, 0, 127), 0, 127).toInt
  }
}


