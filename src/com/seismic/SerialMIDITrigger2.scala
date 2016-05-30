package com.seismic

import processing.core.{PApplet, PConstants, PFont}
import processing.core.PApplet.constrain
import com.seismic.p.PAppletRunner
import themidibus.MidiBus
import com.seismic.p.ProcessingUtils.map
import com.seismic.utils.RandomHelper.{pick, random}

object SerialMIDITrigger2 {
  def main(args: Array[String]): Unit = {
    PAppletRunner.run(new SerialMIDITrigger2)
  }
}

case class Location(x: Int, y: Int)

case class PText(location: Location) {
  var text: Option[String] = None

  def set(text: String): Unit = {
    this.text = Option(text)
  }

  def render(canvas: PApplet): Unit = {
    text match {
      case Some(text) =>
        canvas.text(text, location.x, location.y)
      case _ =>
    }
  }
}

class SerialMIDITrigger2 extends PApplet {

  var seismic: Seismic = null
  var font: PFont = null
  val messageSource = new MessageSource

  val triggerOnDisplays = Map(
    "KICK" -> PText(Location(10, 40)),
    "SNARE" -> PText(Location(10, 70))
  )

  override def settings(): Unit = {
    size(800, 600)
  }

  override def setup(): Unit = {
    // TODO: reconnect button so you don't have to restart app
    val midiBus = new MidiBus(this, -1, "IAC Bus 2");
    seismic = new Seismic(midiBus)
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

class MessageSource {

  def nextMessage = {
    val s = new StringBuilder
    s.append(pick("ON", "OFF"))
    s.append(",")
    s.append(pick("KICK", "SNARE"))
    s.append(",")
    s.append(random.nextInt(1023))
    s.append(",")
    s.append(random.nextInt(1023))
    s.toString
  }
}

class Seismic(midiBus: MidiBus) {

  val builder = new MIDIInstrumentBuilder(midiBus)

  // TODO: if I want to change instruments, I have to change these arrays, which means changing the TrigerMap which means changing et cetc etc
  //
  // So, if I want song A to be 3 instruments, Song B to be 2, and Song C to be 7, I have to create new maps each time.
  // So, maybe I should adjust the grouping of what is changeable to things that change via knob and things that
  // change via song change (foot controller? Or maybe not!)
  //
  // A song change might be a button or select in the UI: which midi banks/channels/mappings do I use for this song?
  val kickInstruments = Array(
    builder.instrument(0),
    builder.instrument(1),
    builder.instrument(2)
  )

  val snareInstruments = Array(
    builder.instrument(3),
    builder.instrument(4),
    builder.instrument(5)
  )

  // TODO: I want to change thresholds or change midi channels on the fly depending on the song, or which channels are mapped
  val kickMap = new TriggerMap(100, 800, kickInstruments)
  val snareMap = new TriggerMap(100, 800, snareInstruments)

  val triggeredState = new TriggeredState

  def midiMap(name: String) = name match {
    case "KICK" => kickMap
    case "SNARE" => snareMap
  }

  def trigger(trigger: TriggerOnMessage): Unit = {
    val instrument = midiMap(trigger.name).mapValue(trigger.handleValue)
    instrument.noteOn(trigger.triggerValue)
    triggeredState.triggered(trigger.name, instrument)
  }

  def off(name: String): Unit = {
    triggeredState.lastTriggered(name) match {
      case Some(instrument) => instrument.noteOff
      case None => System.err.println(
        "Somehow managed to trigger an off event with no previous on event for " + name + ". Ignoring.")
    }
  }
}

class TriggerMap[T](from: Int, to: Int, instruments: Array[T]) {
  def mapValue(handleValue: Int) = {
    val idx = map(handleValue, from, to, 0, instruments.length - 1)
    instruments(idx)
  }
}

object TriggerMessageParser {

  def from(message: String) = {
    val values = message.trim().split(",")
    val onOff = values(0)
    val trigger = values(1)

    if (onOff.equals("ON")) {
      val triggerValue = Integer.parseInt(values(2));
      val handleValue = Integer.parseInt(values(3));

      Some(TriggerOnMessage(trigger, triggerValue, handleValue))
    } else if (onOff.equals("OFF")) {
      Some(TriggerOffMessage(trigger))
    } else {
      None
    }

  }
}

case class TriggerOnMessage(name: String, triggerValue: Int, handleValue: Int)
case class TriggerOffMessage(name: String)

// TODO: this should hold state that can be changed? What if we want to change the channel?
// this should hold both, eh?
case class InstrumentTriggerConfig(threshold: Int = 900,
                                   note: Int = 60) {

  def mapValueToVelocity(value: Int) = {
    constrain(map(value, 0, threshold, 0, 127), 0, 127);
  }
}

class MIDIInstrumentBuilder(midiBus: MidiBus) {

  def instrument(channel: Int,
                 config: InstrumentTriggerConfig = new InstrumentTriggerConfig) = {
    new MIDIInstrument(midiBus, channel, config)
  }
}

class TriggeredState {
  var lastTriggeredMap = Map[String, MIDIInstrument]()

  def triggered(name: String, instrument: MIDIInstrument): Unit = {
    lastTriggeredMap = lastTriggeredMap + (name -> instrument)
  }

  def lastTriggered(name: String) = {
    lastTriggeredMap.get(name)
  }
}

class MIDIInstrument(midiBus: MidiBus,
                     channel: Int,
                     config: InstrumentTriggerConfig) {

  def noteOn(value: Int): Unit = {
    midiBus.sendNoteOn(channel, config.note, config.mapValueToVelocity(value));
  }

  def noteOff() {
    midiBus.sendNoteOff(channel, config.note, 0);
  }
}
