package com.seismic.p

import com.seismic.Seismic
import com.seismic.messages.{MessageSource, TriggerMessageParser, TriggerOffMessage, TriggerOnMessage}
import com.seismic.midi.StupidMonkeyMIDI
import processing.core.{PApplet, PConstants, PFont}

object SerialMIDITrigger2 {
  def main(args: Array[String]): Unit = {
    PAppletRunner.run(new SerialMIDITrigger2)
  }
}

class SerialMIDITrigger2 extends PApplet {

  var seismic: Seismic = null
  var font: PFont = null
  val messageSource = new MessageSource
  val midiIO = new StupidMonkeyMIDI("IAC Bus 2");

  val triggerOnDisplays = Map(
    "KICK" -> PText(Location(10, 40)),
    "SNARE" -> PText(Location(10, 70))
  )

  override def settings(): Unit = {
    size(800, 600)
  }

  override def setup(): Unit = {
    // TODO: reconnect button so you don't have to restart app
    seismic = new Seismic(midiIO)
    font = createFont("Menlo-Regular", 13)
    PFont.list().foreach { (s) =>
      println(s)
    }

    frameRate(1800)
  }

  override def draw(): Unit = {
    background(9)
    textAlign(PConstants.LEFT)
    textFont(font)
    fill(255)

    nextMessage match {
      case Some(message) => handleMessage(message)
      case None =>
    }

    triggerOnDisplays.values.foreach { (pText) =>
      pText.render(this)
    }

    text(f"$frameCount%6.6s $frameRate%6.4f", 10, 10)
  }

  def handleMessage(message: String) {
    TriggerMessageParser.from(message) match {
      case Some(triggerOn: TriggerOnMessage) =>
        seismic.trigger(triggerOn)
        displayTrigger(triggerOn)
      case Some(TriggerOffMessage(name)) =>
        seismic.off(name)
      case None =>
        System.out.println("Unknown message: \"" + message + "\"")
    }
  }

  def nextMessage = {
    if (frameCount % 20 != 0) {
      None
    } else {
      Some(messageSource.nextMessage)
    }
  }

  def displayTrigger(triggerOn: TriggerOnMessage): Unit = {
    triggerOnDisplays(triggerOn.name) match {
      case pText: PText =>
        val (name, triggerValue, handleValue) =
          (triggerOn.name, triggerOn.triggerValue, triggerOn.handleValue)
        pText.set(f"$name%5.5s $triggerValue%4.4s $handleValue%4.4s")

      case _ => System.err.println(String.format("Unknown trigger: %s", triggerOn.name))

    }
  }
}
