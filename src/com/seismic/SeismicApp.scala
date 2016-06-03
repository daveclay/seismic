package com.seismic

import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue}
import java.util.concurrent.locks.LockSupport

import com.seismic.messages._
import com.seismic.midi.StupidMonkeyMIDI
import com.seismic.p.{PAppletRunner, SeismicUI}
import com.seismic.serial.SerialMonitor

object SeismicApp {
  def main(args: Array[String]): Unit = {
    val serialMonitor = new SerialMonitor
    val midiIO = new StupidMonkeyMIDI("IAC Bus 2")
    val seismic = new Seismic(midiIO)
    val seismicUI = new SeismicUI

    val seismicMidiHandler = (message: Message) => {
      message match {
        case triggerOn: TriggerOnMessage => seismic.trigger(triggerOn)
        case TriggerOffMessage(name) => seismic.off(name)
      }
    }

    val uiMessageHandler = (message: Message) => {
      seismicUI.handleMessage(message)
    }

    new SeismicMessageHandler(serialMonitor,
                               Array(seismicMidiHandler, uiMessageHandler))

    seismicUI.start()
    serialMonitor.start("mock")
  }
}

class SeismicMessageHandler(serialMonitor: SerialMonitor,
                            handlers: Array[(Message => Unit)]) {


  serialMonitor.setHandler { (message: String) =>
    TriggerMessageParser.from(message) match {
      case Some(message: Message) =>
        handlers.foreach { (handler) => handler(message) }

      case _ => System.out.println("Unknown message: \"" + message + "\"")
    }
  }
}
