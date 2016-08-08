package com.seismic

import com.seismic.io.Preferences
import com.seismic.messages._
import com.seismic.midi.StupidMonkeyMIDI
import com.seismic.serial.{SerialMonitor, StandardSerialIO}
import com.seismic.ui.swing.{SeismicSerialCallbacks, SeismicUI, SeismicUIFactory}
import com.seismic.ui.swing.SwingThreadHelper.invokeLater

object SeismicApp {

  def main(args: Array[String]): Unit = {
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Seismic")

    if (args.length < 1) {
      println("java -jar seismic.jar [serial port] [MIDI port]")
      println("Available serial ports:")
      StandardSerialIO.list().foreach { p => println(p) }
      println("\nAvailable Midi Busses:")
      StupidMonkeyMIDI.availableInputs().foreach { p => println(p) }
      return
    }

    val serialMonitor = new SerialMonitor
    val midiIO = new StupidMonkeyMIDI("IAC Bus 2")
    val preferences = Preferences.getPreferences
    val triggeredState = new TriggeredState
    val seismic = new Seismic(midiIO, preferences, triggeredState)
    val seismicUIFactory = new SeismicUIFactory

    val seismicMidiHandler = (message: Message) => {
      message match {
        case triggerOn: TriggerOnMessage => seismic.trigger(triggerOn)
        case TriggerOffMessage(name) => seismic.off(name)
        case patchMessage: PatchMessage => seismic.patch(patchMessage.patch)
        case nextPhrase: NextPhraseMessage => seismic.selectNextPhrase()
        case previousPhrase: PreviousPhraseMessage => seismic.selectPreviousPhrase()
        case _ => System.out.println(s"Ignoring unknown message $message")
      }
    }

    invokeLater { () =>
      val nextPhrase = () => {
        serialMonitor.fireSerialMessage(TriggerMessageParser.nextPhrase)
      }
      val prevPhrase = () => {
        serialMonitor.fireSerialMessage(TriggerMessageParser.prevPhrase)
      }
      val patch = (patch: Int) => {
        serialMonitor.fireSerialMessage(TriggerMessageParser.patch(patch))
      }
      val triggerOn = (name: String, triggerValue: Int, handleValue: Int) => {
        serialMonitor.fireSerialMessage(TriggerMessageParser.triggerOn(name, triggerValue, handleValue))
      }
      val triggerOff = (name: String) => {
        serialMonitor.fireSerialMessage(TriggerMessageParser.triggerOff(name))
      }

      val callbacks = SeismicSerialCallbacks(triggerOn,
                                              triggerOff,
                                              prevPhrase,
                                              nextPhrase,
                                              patch)

      // TODO: fuck the factory, just build the goddamned UI and don't do shit until it's (Graphics2D) loaded.
      val seismicUI = seismicUIFactory.build(seismic, callbacks)

      val uiMessageHandler = (message: Message) => {
        // TODO: seismicUI.getMessageHandlers() ? seismicUI.registerHandlers(serialMonitor) ?
        // TODO: ui might go away if the UI just listens to seismic, not the actual messages from SerialMonitor
        seismicUI.triggerMonitorUI.handleMessage(message)
      }

      val handlers = Array(seismicMidiHandler, uiMessageHandler)

      serialMonitor.setHandler { (value: String) =>
        try {
          val message = TriggerMessageParser.from(value)
          handlers.foreach { (handler) => handler(message) }
        } catch {
          case iae: IllegalArgumentException => iae.printStackTrace()
        }
      }

      serialMonitor.start(args(0))
      midiIO.addOutput(args(1))
    }
  }
}
