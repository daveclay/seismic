package com.seismic.messages

trait Message
case class TriggerOnMessage(name: String, triggerValue: Int, handleValue: Int) extends Message
case class TriggerOffMessage(name: String) extends Message

