package com.seismic.p

import com.seismic.messages.{TriggerOffMessage, TriggerOnMessage}
import controlP5.ControlP5
import processing.core.PApplet

/**
  * A thing that displays some information about trigger messages via text and a level meter
  *
  * @param text
  * @param meter
  */
class InstrumentMonitor(val text: PText,
                        val meter: Meter) {

  def setup(controlP5: ControlP5): Unit = {
    meter.setup(controlP5)
  }

  def render(canvas: PApplet): Unit = {
    text.render(canvas)
    meter.render(canvas)
  }

  def handleTriggerOn(triggerOn: TriggerOnMessage) = {
    val triggerValue = triggerOn.triggerValue
    meter.set(triggerValue)
    text.set(String.format("%5.5s %4.4s %4.4s",
                            triggerOn.name,
                            triggerValue.toString,
                            triggerOn.handleValue.toString))
  }

  def handleTriggerOff(triggerOff: TriggerOffMessage) = {
    meter.off()
  }
}
