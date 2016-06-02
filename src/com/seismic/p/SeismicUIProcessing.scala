package com.seismic.p

import com.seismic.SeismicRouter
import com.seismic.messages._
import com.seismic.p.MeterFactory.buildMeter
import controlP5.ControlP5
import processing.core.{PApplet, PConstants, PFont}

class SeismicUIProcessing(seismicRouter: SeismicRouter) extends PApplet {

  var canvas = this
  var ptMono11: PFont = null
  var ptMono13: PFont = null
  var controlP5: ControlP5 = null

  val handleMonitor = new HandleMonitor(
    text = PText(Location(10, 140)),
    meter = buildMeter(Location(100, 400), canvas))

  val kickMonitor = new InstrumentMonitor(
     text = PText(Location(10, 40)),
     meter = buildMeter(Location(145, 20), canvas))

  val snareMonitor = new InstrumentMonitor(
    text = PText(Location(10, 85)),
    meter = buildMeter(Location(145, 60), canvas))

  val triggerMonitors = Map(
    "KICK" -> kickMonitor,
    "SNARE" -> snareMonitor
  )

  val handleMessage = (message: Message) => {
    message match {
      case triggerOn: TriggerOnMessage => handleTriggerOn(triggerOn)
      case triggerOff: TriggerOffMessage => handleTriggerOff(triggerOff)
      case _ =>
    }
  }

  override def settings(): Unit = {
    size(800, 600)
  }

  override def setup(): Unit = {
    ptMono11 = createFont("PTMono-Regular", 11)
    ptMono13 = createFont("PTMono-Regular", 13)

    controlP5 = new ControlP5(this, ptMono11)
    controlP5.setAutoDraw(false)

    triggerMonitors.values.foreach { (monitor) => monitor.setup(controlP5) }

    // be conservative; all the timing-sensitive stuff is happening on other threads, so the animation
    // is the least of our concerns.
    frameRate(30)
  }

  override def draw(): Unit = {
    background(50, 50, 60)
    textAlign(PConstants.LEFT)
    textFont(ptMono13)
    fill(255)

    controlP5.draw()

    updateMessageData()

    triggerMonitors.values.foreach { (monitor) => monitor.render(this) }

    text(f"$frameCount%6.6s $frameRate%6.4f", 10, 10)
    text(f"mouse x: $mouseX%4d y: $mouseY%4d", 10, 500)
  }

  def updateMessageData(): Unit = {
    seismicRouter.drainMessages { (msg) => handleMessage(msg) }
  }

  private def handleTriggerOn(triggerOn: TriggerOnMessage) = {
    handleMonitor.handleTriggerOn(triggerOn)
    triggerMonitors(triggerOn.name) match {
      case monitor: InstrumentMonitor => monitor.handleTriggerOn(triggerOn)
      case _ => System.err.println(String.format("Unknown trigger: %s", triggerOn.name))
    }
  }

  private def handleTriggerOff(triggerOff: TriggerOffMessage) = {
    val name = triggerOff.name
    triggerMonitors(name) match {
      case monitor: InstrumentMonitor => monitor.handleTriggerOff(triggerOff)
      case _ => System.err.println(String.format("Unknown trigger: %s", name))
    }
  }
}

