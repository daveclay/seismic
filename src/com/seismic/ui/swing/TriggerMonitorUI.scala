package com.seismic.ui.swing

import java.awt.{Color, Dimension, Font}
import javax.swing.JPanel

import com.seismic.ui.swing.SwingThreadHelper.invokeLater
import com.daveclay.swing.util.Position._
import com.seismic.messages.{Message, TriggerOffMessage, TriggerOnMessage}

class TriggerMonitorUI(monoFont: Font, size: Dimension) extends JPanel {
  SwingComponents.addBorder(this)
  setFocusable(false)
  setPreferredSize(size)

  val monitorSize = new Dimension(size.width / 2 - 100, size.height - 8)

  val kickMonitor = new TriggerMeter("KICK", monoFont, monitorSize)
  val snareMonitor = new TriggerMeter("SNARE", monoFont, monitorSize)
  val handleMeter = new HandleMeter(new Dimension(140, size.height - 8))

  val triggerMonitors = Map(
                             "KICK" -> kickMonitor,
                             "SNARE" -> snareMonitor)

  position(kickMonitor).at(4, 4).in(this)
  position(snareMonitor).toTheRightOf(kickMonitor).withMargin(4).in(this)
  position(handleMeter).toTheRightOf(snareMonitor).withMargin(10).in(this)

  override def setBackground(color: Color): Unit = {
    super.setBackground(color)
    if (handleMeter != null) {
      handleMeter.setBackground(color)
    }
  }

  def handleMessage(message: Message): Unit = {
    // TODO: shouldn't this just listen to Seismic, not for raw messages?
    invokeLater { () =>
      message match {
        case triggerOn: TriggerOnMessage => handleTriggerOn(triggerOn)
        case triggerOff: TriggerOffMessage => handleTriggerOff(triggerOff)
        case _ =>
      }
    }
  }

  private def handleTriggerOn(triggerOn: TriggerOnMessage): Unit = {
    triggerMonitors.get(triggerOn.name) match {
      case Some(monitor) =>
        val value = triggerOn.triggerValue
        monitor.setValue(value)
      case None =>
    }
    handleMeter.setRawValue(triggerOn.handleValue)
  }

  private def handleTriggerOff(triggerOff: TriggerOffMessage): Unit = {
    triggerMonitors.get(triggerOff.name) match {
      case Some(monitor) =>
        monitor.off()
      case None =>
    }
  }
}
