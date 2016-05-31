package com.seismic.p

import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue}

import com.seismic.Seismic
import com.seismic.messages.{MessageSource, TriggerMessageParser, TriggerOffMessage, TriggerOnMessage}
import com.seismic.midi.StupidMonkeyMIDI
import com.seismic.serial.SerialMonitor
import processing.core.{PApplet, PConstants, PFont}

object SeismicApp {
  def main(args: Array[String]): Unit = {
    PAppletRunner.run(new SeismicApp)
  }
}

class SeismicApp extends PApplet {

  var font: PFont = null
  val triggerOnDisplays = Map(
    "KICK" -> PText(Location(10, 40)),
    "SNARE" -> PText(Location(10, 70))
  )
  val renderQueue = new ArrayBlockingQueue[(PApplet) => Unit](100);

  val messageHandler = (message: String) => {
      // Note: this is NOT called on the animation thread!
      TriggerMessageParser.from(message) match {
        case Some(triggerOn: TriggerOnMessage) =>
          seismic.trigger(triggerOn)
          // notify the processing SeismicApp to render the trigger
          renderQueue.add((canvas) => updateTriggerOnDisplay(triggerOn))
        case Some(TriggerOffMessage(name)) =>
          seismic.off(name)
        case None =>
          System.out.println("Unknown message: \"" + message + "\"")
      }
    }

  val messageSource = new MessageSource
  val midiIO = new StupidMonkeyMIDI("IAC Bus 2");
  val serialMonitor = new SerialMonitor(messageHandler)
  val seismic = new Seismic(midiIO)

  override def settings(): Unit = {
    size(800, 600)
  }

  override def setup(): Unit = {
    // TODO: reconnect button so you don't have to restart app when things get unplugged?
    serialMonitor.start("mock")

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

    drawRenderQueue();

    triggerOnDisplays.values.foreach { (pText) =>
      pText.render(this)
    }

    text(f"$frameCount%6.6s $frameRate%6.4f", 10, 10)
  }

  def drawRenderQueue(): Unit = {
    import scala.collection.JavaConversions._
    renderQueue.iterator().foreach { (renderable) => renderable(this) }
  }

  def updateTriggerOnDisplay(triggerOn: TriggerOnMessage): Unit = {
    triggerOnDisplays(triggerOn.name) match {
      case pText: PText =>
        pText.set(String.format("%5.5s %4.4s %4.4s",
          triggerOn.name, triggerOn.triggerValue.toString, triggerOn.handleValue.toString))

      case _ => System.err.println(String.format("Unknown trigger: %s", triggerOn.name))
    }
  }
}
