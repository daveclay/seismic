package com.seismic.messages

/**
  * Trigger On:   "T,ON,KICK,203,152"
  * Trigger Off:  "T,OFF,KICK"
  * Patch:        "PATCH,7"
  * Phrase Prev:  "PHRASE,PREV"
  * Phrase Next:  "PHRASE,NEXT"
  */
object TriggerMessageParser {
  def nextPhrase = "PHRASE,NEXT"
  def prevPhrase = "PHRASE,PREV"
  def patch(patch: Int) = "PATCH,$patch"
  def triggerOn(name: String, triggerValue: Int, handleValue: Int, fingerValue: Int) = s"T,ON,$name,$triggerValue,$handleValue,$fingerValue"
  def triggerOff(name: String) = s"T,OFF,$name"

  def from(message: String): Message = {
    val values = message.trim().split(",")
    values.head match {
      case "T" => parseTrigger(values.drop(1))
      case "PATCH" => parsePatch(values.last)
      case "PHRASE" => parsePhrase(values.last)
      case _ => throw new IllegalArgumentException(s"Unknown message $message")
    }
  }

  /**
    * Patch:        "PATCH,7"
    * @param patchString
    * @return
    */
  private def parsePatch(patchString: String) = {
    PatchMessage(Integer.parseInt(patchString))
  }

  /**
    * Phrase Prev:  "PHRASE,PREV"
    * Phrase Next:  "PHRASE,NEXT"
    * @param dir
    * @return
    */
  private def parsePhrase(dir: String) = {
    dir match {
      case "PREV" => PreviousPhraseMessage()
      case "NEXT" => NextPhraseMessage()
      case _ => throw new IllegalArgumentException(s"Unknown phrase message $dir")
    }
  }

  /**
    * Trigger On:   "T,ON,KICK,203,152,0"
    * Trigger Off:  "T,OFF,KICK"
    * @param values
    * @return
    */
  private def parseTrigger(values: Array[String]) = {
    val onOff = values(0)
    val trigger = values(1)

    onOff match {
      case "ON" =>
        val triggerValue = Integer.parseInt(values(2))
        val handleValue = Integer.parseInt(values(3))
        val fingerTrigger = Integer.parseInt(values(4)) == 1
        TriggerOnMessage(trigger, triggerValue, handleValue, fingerTrigger)
      case "OFF" => TriggerOffMessage(trigger)
      case _ => throw new IllegalArgumentException(s"Invalid message: $onOff in ${values.mkString(",")}")
    }
  }
}
