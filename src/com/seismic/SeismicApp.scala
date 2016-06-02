package com.seismic

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.locks.LockSupport

import com.seismic.messages._
import com.seismic.midi.StupidMonkeyMIDI
import com.seismic.p.{PAppletRunner, SeismicUI}
import com.seismic.serial.SerialMonitor

object SeismicApp {
  def main(args: Array[String]): Unit = {
    val seismicRouter = new SeismicRouter
    // PAppletRunner.run(new SeismicUI(seismicRouter))

    seismicRouter.start("mock")
    LockSupport.park(this)
  }
}

class SeismicRouter {
  val messageQueue = new ArrayBlockingQueue[Message](100)

  val messageHandler = (message: String) => {
    // Note: this is NOT called on the animation thread!
    TriggerMessageParser.from(message) match {
      case Some(triggerOn: TriggerOnMessage) => handleTriggerOn(triggerOn)
      case Some(triggerOff: TriggerOffMessage) => handleTriggerOff(triggerOff)
      case _ => System.out.println("Unknown message: \"" + message + "\"")
    }
  }

  val midiIO = new StupidMonkeyMIDI("IAC Bus 2")
  val serialMonitor = new SerialMonitor(messageHandler)
  val seismic = new Seismic(midiIO)

  def start(port: String): Unit = {
    // TODO: reconnect button so you don't have to restart app when things get unplugged?
    serialMonitor.start(port)
  }

  def drainMessages(handleMessage: (Message => Unit)) = {
    import scala.collection.JavaConversions._
    // TODO: hide this bullshit
    val messages = new java.util.ArrayList[Message]()
    messageQueue.drainTo(messages)
    messages.foreach { (msg) => handleMessage(msg) }
  }

  private def handleTriggerOn(triggerOn: TriggerOnMessage) = {
    seismic.trigger(triggerOn)
    messageQueue.add(triggerOn)
  }

  private def handleTriggerOff(triggerOff: TriggerOffMessage) = {
    val name = triggerOff.name
    seismic.off(name)
    messageQueue.add(triggerOff)
  }

}
