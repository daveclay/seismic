package com.seismic.ui

import java.awt.{Color, Dimension, Insets}
import javax.swing.JPanel

import com.daveclay.swing.util.Position._
import com.seismic.io.Preferences
import com.seismic.messages.{Message, TriggerOffMessage, TriggerOnMessage}
import com.seismic.ui.utils.SwingComponents
import com.seismic.ui.utils.SwingThreadHelper.invokeLater
import com.seismic.ui.utils.layout.GridBagLayoutHelper

class TriggerMonitorUI(onKickThresholdSet: (Int) => Unit,
                       onSnareThresholdSet: (Int) => Unit,
                       size: Dimension) extends JPanel {
  SwingComponents.addBorder(this)
  setFocusable(false)
  setPreferredSize(size)
  setMinimumSize(size)

  private val triggerThresholds = Preferences.getPreferences.triggerThresholds

  private val monitorSize = new Dimension(size.width / 2 - 100, size.height - 8)
  private val kickMonitor = new TriggerMeter("KICK",
                                              onKickThresholdSet,
                                              triggerThresholds.kickThreshold,
                                              monitorSize)

  private val snareMonitor = new TriggerMeter("SNARE",
                                               onSnareThresholdSet,
                                               triggerThresholds.snareThreshold,
                                               monitorSize)

  private val handleMeter = new HandleMeter(new Dimension(194, size.height - 8))

  private val triggerMonitors = Map("KICK" -> kickMonitor, "SNARE" -> snareMonitor)

  val helper = new GridBagLayoutHelper(this)

  helper.position(kickMonitor).atOrigin().withPadding(new Insets(4, 4, 0, 0)).alignLeft().fillHorizontal().weightX(.5f).inParent()
  helper.position(snareMonitor).nextTo(kickMonitor).alignLeft().fillHorizontal().weightX(.5f).withPadding(new Insets(4, 4, 0, 4)).inParent()
  helper.position(handleMeter).nextTo(snareMonitor).alignLeft().withPadding(new Insets(4, 0, 4, 0)).inParent()

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
