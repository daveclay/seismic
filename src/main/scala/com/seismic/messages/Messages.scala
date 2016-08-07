package com.seismic.messages

trait Message
case class TriggerOnMessage(name: String, triggerValue: Int, handleValue: Int) extends Message
case class TriggerOffMessage(name: String) extends Message
case class PatchMessage(patch: Int) extends Message
case class NextPhraseMessage() extends Message
case class PreviousPhraseMessage() extends Message

