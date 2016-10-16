package com.seismic.ui

import java.awt._
import java.awt.event.{MouseAdapter, MouseEvent}
import java.util.concurrent.{Executors, ScheduledFuture, TimeUnit}
import javax.swing.JPanel

import com.daveclay.swing.util.Position._
import com.seismic.ui.utils.SwingThreadHelper.invokeLater
import com.seismic.ui.utils.layout.GridBagLayoutHelper
import com.seismic.ui.utils.{Indicator, LabeledTextField, SwingComponents}

class TriggerMeter(title: String,
                   onThresholdSet: (Int) => Unit,
                   threshold: Int,
                   size: Dimension) extends JPanel {
  setPreferredSize(size)
  setMinimumSize(size)
  setBackground(Color.BLACK)

  var decayCounter = 30
  val decayTick = 10
  val scheduledThreadExecutor = Executors.newScheduledThreadPool(1)
  var currentValue = 0
  var currentDecayFuture: Option[ScheduledFuture[_]] = None

  val decayRunnable = new Runnable() {
    def run(): Unit = {
      if (currentValue > 0) {
        currentValue = Math.max(0, currentValue - decayCounter)
        invokeLater { () =>
          setValueWithoutDecay(currentValue)
        }
        scheduleNextDecay()
      } else {
        cancelDecay()
      }
    }
  }

  private val label = SwingComponents.label(title)
  label.setForeground(Color.WHITE)

  private val onThresholdValueSet = (v: String) => onThresholdSet(v.toInt)
  private val thresholdField = new LabeledTextField("lim", 5, LabeledTextField.Vertical, 0, onThresholdValueSet)
  thresholdField.setText(threshold.toString)
  thresholdField.setPreferredSize(SwingComponents.digitFieldDimension())
  thresholdField.setMinimumSize(SwingComponents.digitFieldDimension())
  thresholdField.inputField.setBackground(SwingComponents.componentBGColor)

  private val linearIndicator = new Indicator(new Dimension(size.width - 100, size.height))
  linearIndicator.setBackground(new Color(40, 40, 50))
  SwingComponents.addBorder(linearIndicator)

  private val labelDimension = new Dimension(140, 21)
  label.setMinimumSize(labelDimension)
  label.setPreferredSize(labelDimension)

  val helper = new GridBagLayoutHelper(this)

  helper.position(linearIndicator).atOrigin().rowspan(2).fillHorizontal().weightX(1).alignLeft().inParent()
  helper.position(label).nextTo(linearIndicator).withPadding(new Insets(4, 4, 0, 4)).alignLeft().inParent()
  helper.position(thresholdField).below(label).withPadding(new Insets(0, 4, 4, 4)).alignLeft().inParent()

  addMouseListener(new MouseAdapter() {
    override def mousePressed(e: MouseEvent): Unit = setValue(1023)
    override def mouseReleased(e: MouseEvent): Unit = off()
  })

  def off() = {
    startDecay()
    label.setText(f"$title%5s  0FF")
    label.setForeground(Color.WHITE)
    setBackground(Color.BLACK)
  }

  def setValue(value: Int): Unit = {
    cancelDecay()
    label.setText(f"$title%5s $value%4s")
    label.setForeground(Color.BLACK)
    setBackground(SwingComponents.highlightColor)
    setValueWithoutDecay(value)
    decayCounter = (value * .04f).toInt
  }

  private def setValueWithoutDecay(value: Int): Unit = {
    currentValue = value
    linearIndicator.setValue(value)
  }

  private def startDecay(): Unit = {
    cancelDecay()
    scheduleNextDecay()
  }

  private def scheduleNextDecay(): Unit = {
    currentDecayFuture = Some(scheduledThreadExecutor.schedule(decayRunnable, decayTick, TimeUnit.MILLISECONDS))
  }

  private def cancelDecay(): Unit = {
    currentDecayFuture match {
      case Some(future) => future.cancel(true)
      case None =>
    }
  }
}

