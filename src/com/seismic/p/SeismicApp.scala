package com.seismic.p

import com.seismic.Seismic
import com.seismic.messages.{MessageSource, TriggerMessageParser, TriggerOffMessage, TriggerOnMessage}
import com.seismic.midi.StupidMonkeyMIDI
import com.seismic.serial.{SerialMessageHandler, SerialMonitor}
import processing.core.{PApplet, PConstants, PFont}

object SeismicApp {
  def main(args: Array[String]): Unit = {
    PAppletRunner.run(new SeismicApp)
  }
}

class SeismicApp extends PApplet {

  var seismic: Seismic = null
  var font: PFont = null
  val messageSource = new MessageSource
  val midiIO = new StupidMonkeyMIDI("IAC Bus 2");
  val serialMonitor = new SerialMonitor("/dev/xxx")

  val triggerOnDisplays = Map(
    "KICK" -> PText(Location(10, 40)),
    "SNARE" -> PText(Location(10, 70))
  )

  override def settings(): Unit = {
    size(800, 600)
  }

  override def setup(): Unit = {
    // TODO: reconnect button so you don't have to restart app when things get unplugged?
    seismic = new Seismic(midiIO)
    serialMonitor.start(new SerialMessageHandler {
      override def handleMessage(message: String): Unit = {
        // Note: this is NOT called on the animation thread!
        TriggerMessageParser.from(message) match {
          case Some(triggerOn: TriggerOnMessage) =>
            seismic.trigger(triggerOn)
          case Some(TriggerOffMessage(name)) =>
            seismic.off(name)
          case None =>
            System.out.println("Unknown message: \"" + message + "\"")
        }
      }
    })

    font = createFont("Menlo-Regular", 13)
    PFont.list().foreach { (s) =>
      println(s)
    }

    // be conservative; all the timing-sensitive stuff is happening on other threads, so the animation
    // is the least of our concerns.
    frameRate(30)
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
        displayTrigger(triggerOn)
      case Some(TriggerOffMessage(name)) =>
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
