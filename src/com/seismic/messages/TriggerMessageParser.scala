package com.seismic.messages

import TriggerOffMessage

object TriggerMessageParser {

  def from(message: String) = {
    val values = message.trim().split(",")
    val onOff = values(0)
    val trigger = values(1)

    if (onOff.equals("ON")) {
      val triggerValue = Integer.parseInt(values(2));
      val handleValue = Integer.parseInt(values(3));

      Some(TriggerOnMessage(trigger, triggerValue, handleValue))
    } else if (onOff.equals("OFF")) {
      Some(TriggerOffMessage(trigger))
    } else {
      None
    }

  }
}
