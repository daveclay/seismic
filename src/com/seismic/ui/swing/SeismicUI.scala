package com.seismic.ui.swing

import java.awt._
import javax.swing.{JFrame, JLabel, JPanel}

import com.daveclay.swing.util.Position.position
import com.daveclay.swing.util.Size.setPreferredSize
import com.seismic.messages._
import com.seismic.ui.swing.SwingThreadHelper.invokeLater
import com.seismic.ui.swing.FontHelper._

class SeismicUI {

  val frame = new JFrame("Seismic")
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  val mainPanel = frame.getContentPane
  val titleFont = new Font("Arial", Font.PLAIN, 18)
  val monoFont = new Font("PT Mono", Font.PLAIN, 18)
  val title = new JLabel("Seismic")
  val kickMonitor = new JLabel
  val snareMonitor = new JLabel
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
        monitor.repaint()
      case None =>
    }
  }

  private def handleTriggerOff(triggerOff: TriggerOffMessage): Unit = {
    triggerMonitors.get(triggerOff.name) match {
      case Some(monitor) =>
        val name = triggerOff.name
        monitor.setText(f"$name%5s  0FF")
        monitor.repaint()
      case None =>
    }
  }

  private def configureMonitors(monitors: JLabel*) = {
    monitors.foreach { (monitor) =>
      monitor.setFont(monoFont)
      monitor.setForeground(new Color(200, 200, 210))
      monitor.setBackground(new Color(200, 80, 80))
      monitor.setOpaque(true)
      val graphics = frame.getGraphics
      val height = monoFont.getHeight(graphics)
      setPreferredSize(monitor, 200, height)
    }
  }

  private def layout(): Unit = {
    setPreferredSize(frame, 600, 300)
    mainPanel.setBackground(new Color(50, 50, 60))

    title.setFont(titleFont)
    title.setForeground(new Color(200, 200, 210))

    configureMonitors(kickMonitor, snareMonitor)

    position(title).at(4, 4).in(mainPanel)
    position(kickMonitor).below(title).withMargin(5).in(mainPanel)
    position(snareMonitor).below(kickMonitor).withMargin(5).in(mainPanel)
  }
}





