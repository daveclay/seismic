package com.seismic.ui.swing

import java.awt._
import java.util.concurrent.Executors
import javax.swing.border.EtchedBorder
import javax.swing._

import com.daveclay.swing.color.GradientValueMap
import com.daveclay.swing.util.Position.position
import com.daveclay.swing.util.Size.setPreferredSize
import com.seismic.messages._
import com.seismic.ui.swing.SwingThreadHelper.invokeLater
import com.seismic.ui.swing.FontHelper._
import com.seismic.utils.ValueMapHelper
import com.seismic.utils.ValueMapHelper.map

class SeismicUIFactory {
  var seismicUIOpt: Option[SeismicUI] = None

  def build() = {
    val frame = new JFrame("Seismic")
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.pack()

    val seismicUI = new SeismicUI(frame, frame.getGraphics)
    frame.pack()
    frame.setVisible(true)

    seismicUIOpt = Some(seismicUI)

    seismicUI
  }

  def handleMessage(message: Message): Unit = {
    seismicUIOpt match {
      case Some(seismicUI) =>
        invokeLater { () =>
          seismicUI.handleMessage(message)
        }
      case None => System.out.println("UI Not loaded yet...")
    }
  }
}

class SeismicUI(frame: JFrame, graphics: Graphics) {
  val backgroundColor = new Color(50, 50, 60)
  val mainPanel = frame.getContentPane
  val titleFont = new Font("Arial", Font.PLAIN, 23)
  val monoFont = new Font("PT Mono", Font.PLAIN, 11)
  val title = new JLabel("SEISMIC")
  val kickMonitor = new Meter("KICK", monoFont, new Dimension(300, 30))
  val snareMonitor = new Meter("SNARE", monoFont, new Dimension(300, 30))
  val triggerMonitors = Map(
    "KICK" -> kickMonitor,
    "SNARE" -> snareMonitor)

  setPreferredSize(frame, 800, 300)
  mainPanel.setBackground(backgroundColor)

  title.setFont(titleFont)
  title.setForeground(new Color(200, 200, 210))

  position(title).at(4, 4).in(mainPanel)
  position(kickMonitor).below(title).withMargin(5).in(mainPanel)
  position(snareMonitor).toTheRightOf(kickMonitor).withMargin(5).in(mainPanel)

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
        monitor.setValue(value)
        monitor.repaint()
      case None =>
    }
  }

  private def handleTriggerOff(triggerOff: TriggerOffMessage): Unit = {
    triggerMonitors.get(triggerOff.name) match {
      case Some(monitor) =>
        val name = triggerOff.name
        monitor.off()
        monitor.repaint()
      case None =>
    }
  }
}


