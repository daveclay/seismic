package com.seismic.ui.swing

import java.awt._
import javax.swing.border.EtchedBorder
import javax.swing.{BorderFactory, JFrame, JLabel, JPanel}

import com.daveclay.swing.util.Position.position
import com.daveclay.swing.util.Size.setPreferredSize
import com.seismic.messages._
import com.seismic.ui.swing.SwingThreadHelper.invokeLater
import com.seismic.ui.swing.FontHelper._
import com.sun.java.swing.plaf.motif.MotifBorders.BevelBorder

import eu.hansolo.steelseries.gauges.Radial;

class SeismicUI {

  val frame = new JFrame("Seismic")
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  val backgroundColor = new Color(50, 50, 60)
  val gauge = new Radial()
  val mainPanel = frame.getContentPane
  val titleFont = new Font("PT Mono", Font.PLAIN, 18)
  val monoFont = new Font("PT Mono", Font.PLAIN, 18)
  val title = new JLabel("S E I S M I C")
  val kickMonitor = new JLabel("KICK")
  val snareMonitor = new JLabel("SNARE")
  val triggerMonitors = Map(
    "KICK" -> kickMonitor,
    "SNARE" -> snareMonitor)

  def start(): Unit = {
    frame.pack()
    layout()
    frame.pack()
    frame.setVisible(true)
  }

  def handleMessage(message: Message): Unit = {
    invokeLater { () =>
      System.out.println(Thread.currentThread().getName + " with message " + message)
      message match {
        case triggerOn: TriggerOnMessage => handleTriggerOn(triggerOn)
        case triggerOff: TriggerOffMessage => handleTriggerOff(triggerOff)
        case _ => System.out.println(f"Unknown message: $message")
      }
    }
  }

  private def handleTriggerOn(triggerOn: TriggerOnMessage): Unit = {
    triggerMonitors.get(triggerOn.name) match {
      case Some(monitor) =>
        val value = triggerOn.triggerValue
        val name = triggerOn.name
        monitor.setText(f"$name%5s $value%4s")
        gauge.setValueAnimated(value)
        monitor.setForeground(new Color(0, 10, 10))
        monitor.setBackground(new Color(250, 130, 30))
        monitor.repaint()
      case None =>
    }
  }

  private def handleTriggerOff(triggerOff: TriggerOffMessage): Unit = {
    triggerMonitors.get(triggerOff.name) match {
      case Some(monitor) =>
        val name = triggerOff.name
        monitor.setText(f"$name%5s  0FF")
        gauge.setValueAnimated(0)
        monitor.setForeground(new Color(100, 100, 100))
        monitor.setBackground(backgroundColor)
        monitor.repaint()
      case None =>
    }
  }

  private def configureMonitors(monitors: JLabel*) = {
    monitors.foreach { (monitorLabel) =>
      monitorLabel.setFont(monoFont)
      monitorLabel.setOpaque(true)
      monitorLabel.setForeground(new Color(100, 100, 100))
      monitorLabel.setBackground(backgroundColor)
      monitorLabel.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)))
      val graphics = frame.getGraphics
      val height = monoFont.getHeight(graphics)
      setPreferredSize(monitorLabel, 120, 120)
    }
  }

  private def layout(): Unit = {
    setPreferredSize(frame, 600, 600)
    mainPanel.setBackground(backgroundColor)

    title.setFont(titleFont)
    title.setForeground(new Color(200, 200, 210))

    configureMonitors(kickMonitor, snareMonitor)

    position(title).at(4, 4).in(mainPanel)
    position(kickMonitor).below(title).withMargin(5).in(mainPanel)
    position(snareMonitor).toTheRightOf(kickMonitor).withMargin(5).in(mainPanel)

    gauge.setTitle("Demo title")
    gauge.setUnitString("Some units")
    gauge.setMinValue(0)
    gauge.setMaxValue(1023)
    gauge.setStdTimeToValue(250)
    gauge.setRtzTimeBackToZero(500)
    setPreferredSize(gauge, 300, 300)

    position(gauge).below(kickMonitor).in(mainPanel)

  }
}





