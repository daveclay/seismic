package com.seismic.ui.swing

import java.awt._
import java.awt.event.{MouseAdapter, MouseEvent, MouseListener}
import java.util.concurrent.{Executors, ScheduledFuture, TimeUnit}
import javax.swing.{JLabel, JPanel}

import com.daveclay.swing.util.Position._
import com.seismic.ui.swing.SwingThreadHelper.invokeLater

class TriggerMeter(title: String,
                   onThresholdSet: (Int) => Unit,
                   threshold: Int,
                   size: Dimension) extends JPanel {
  setPreferredSize(size)
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
  thresholdField.inputField.setBackground(SwingComponents.componentBGColor)

  private val linearIndicator = new Indicator(new Dimension(size.width - 100, size.height))
  linearIndicator.setBackground(new Color(40, 40, 50))
  SwingComponents.addBorder(linearIndicator)

  label.setPreferredSize(new Dimension(140, 21))

  position(linearIndicator).at(0, 0).in(this)
  position(label).toTheRightOf(linearIndicator).withMargin(4).in(this)
  position(thresholdField).below(label).withMargin(4).in(this)

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
    setBackground(new Color(170, 170, 170))
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

