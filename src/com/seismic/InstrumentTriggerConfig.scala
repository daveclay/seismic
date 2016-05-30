package com.seismic

import com.seismic.p.ProcessingUtils.{map => _, _}
import processing.core.PApplet._

// TODO: this should hold state that can be changed? What if we want to change the channel?
// this should hold both, eh?
case class InstrumentTriggerConfig(threshold: Int = 900,
                                   note: Int = 60) {

  def mapValueToVelocity(value: Int) = {
    constrain(map(value, 0, threshold, 0, 127), 0, 127);
  }
}
