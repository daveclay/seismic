package com.seismic

import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue}
import java.util.concurrent.locks.LockSupport

import com.seismic.messages._
import com.seismic.midi.StupidMonkeyMIDI
import com.seismic.serial.SerialMonitor
import com.seismic.ui.swing.{SeismicUI, SeismicUIFactory}
import com.seismic.ui.swing.SwingThreadHelper.invokeLater

object SeismicApp {
  def main(args: Array[String]): Unit = {
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Seismic")
    val serialMonitor = new SerialMonitor
    val midiIO = new StupidMonkeyMIDI("IAC Bus 2")
    val seismic = new Seismic(midiIO)
    val seismicUIFactory = new SeismicUIFactory

    val seismicMidiHandler = (message: Message) => {
      message match {
        case triggerOn: TriggerOnMessage => seismic.trigger(triggerOn)
        case TriggerOffMessage(name) => seismic.off(name)
      }
    }

    invokeLater { () =>
      val seismicUI = seismicUIFactory.build(seismic)
    }

    val uiMessageHandler = (message: Message) => {
      seismicUIFactory.handleMessage(message)
    }

    serialMonitor.setHandler { (message: String) =>
      TriggerMessageParser.from(message) match {
        case Some(message: Message) =>
          Array(seismicMidiHandler, uiMessageHandler).foreach { (handler) => handler(message) }

        case _ => System.out.println("Unknown message: \"" + message + "\"")
      }
    }

    serialMonitor.start("mock")
  }
}
