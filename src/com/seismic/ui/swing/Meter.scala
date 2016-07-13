package com.seismic.ui.swing

import java.awt._
import java.awt.event.{MouseEvent, MouseListener}
import java.util.concurrent.{Executors, ScheduledFuture, TimeUnit}
import javax.swing.{JLabel, JPanel}

import com.daveclay.swing.util.Position._
import com.seismic.ui.swing.SwingThreadHelper.invokeLater

class Meter(title: String,
            font: Font,
            size: Dimension) extends JPanel {

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

  setBackground(new Color(20, 20, 30))

  val label = new JLabel(title)
  label.setFont(font)
  label.setForeground(Color.WHITE)

  val linearIndicator = new Indicator(new Dimension(size.width - 100, size.height))
  linearIndicator.setBackground(new Color(40, 40, 50))

  this.setPreferredSize(size)
  label.setPreferredSize(new Dimension(140, 21))

  position(linearIndicator).at(0, 0).in(this)
  position(label).toTheRightOf(linearIndicator).withMargin(4).in(this)

  addMouseListener(new MouseListener() {
    override def mouseExited(e: MouseEvent): Unit = {}

    override def mouseClicked(e: MouseEvent): Unit = {}

    override def mouseEntered(e: MouseEvent): Unit = {}

    override def mousePressed(e: MouseEvent): Unit = {
      setValue(1023)
    }

    override def mouseReleased(e: MouseEvent): Unit = {
      off()
    }
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

  def setValueWithoutDecay(value: Int): Unit = {
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

