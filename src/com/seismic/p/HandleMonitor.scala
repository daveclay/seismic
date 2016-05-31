package com.seismic.p

import com.seismic.messages.{TriggerOffMessage, TriggerOnMessage}

/**
  * TODO: probably shouldn't be an instrument monitor forever, but meh
  * @param text
  * @param meter
  */
class HandleMonitor(text: PText,
                    meter: Meter) extends InstrumentMonitor(text, meter) {

  override def handleTriggerOn(triggerOn: TriggerOnMessage) = {
    val triggerValue = triggerOn.triggerValue
    meter.set(triggerValue)
    text.set(String.format("HANDLE %4.4s", triggerOn.handleValue.toString))
  }
}
