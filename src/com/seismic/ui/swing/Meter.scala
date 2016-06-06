package com.seismic.ui.swing

import java.awt.event.{MouseEvent, MouseListener}
import java.awt._
import java.util.concurrent.{Executors, ScheduledFuture, TimeUnit}
import javax.swing.{JLabel, JPanel, SwingUtilities}

import com.daveclay.swing.util.Position._
import com.daveclay.swing.util.SwingUtil
import com.seismic.ui.swing.SwingThreadHelper.invokeLater

class Meter(title: String,
            font: Font,
            size: Dimension) extends JPanel {

  var decayCounter = 30
  val decayTick = 30
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

  val triggerIndicator = new JPanel() {
    var active = false
    def activate(): Unit = {
      active = true
    }
    def deactivate(): Unit = {
      active = false
    }
    override def paint(graphics: Graphics): Unit = {
      val g2d = graphics.asInstanceOf[Graphics2D]
      g2d.setColor(new Color(255, 100, 0))
      if (active) {
        g2d.fillOval(0, 0, 9, 9)
      } else {
        g2d.drawOval(0, 0, 9, 9)
      }
    }
  }

  triggerIndicator.setPreferredSize(new Dimension(20, 20))

  val linearIndicator = new Indicator(new Dimension(size.width - 100, size.height))
  linearIndicator.setBackground(new Color(40, 40, 50))

  this.setPreferredSize(size)
  label.setPreferredSize(new Dimension(140, 21))

  position(linearIndicator).at(0, 0).in(this)
  position(label).toTheRightOf(linearIndicator).in(this)
  position(triggerIndicator).below(label).in(this)

  addMouseListener(new MouseListener() {
    override def mouseExited(e: MouseEvent): Unit = {}

    override def mouseClicked(e: MouseEvent): Unit = {}

    override def mouseEntered(e: MouseEvent): Unit = {}

    override def mousePressed(e: MouseEvent): Unit = {
      setValue(1023)
    }

    override def mouseReleased(e: MouseEvent): Unit = {
      setValue(0)
    }
  })

  def off() = {
    startDecay()
    label.setText(f"$title%5s  0FF")
    triggerIndicator.deactivate()
  }

  def setValue(value: Int): Unit = {
    cancelDecay()
    label.setText(f"$title%5s $value%4s")
    triggerIndicator.activate()
    setValueWithoutDecay(value)
    decayCounter = (value * .03f).toInt
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
