package com.seismic.messages

trait Message
case class TriggerOnMessage(name: String, triggerValue: Int, handleValue: Int) extends Message
case class TriggerOffMessage(name: String) extends Message
case class PatchMessage(patch: Int) extends Message
case class PhraseNextMessage() extends Message
case class PhrasePreviousMessage() extends Message

