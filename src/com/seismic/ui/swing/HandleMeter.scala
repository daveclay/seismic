package com.seismic.ui.swing

import java.awt.event.{MouseEvent, MouseListener}
import java.awt.{Color, Dimension, Font, Graphics}
import javax.swing.{JLabel, JLayeredPane}

import com.daveclay.swing.util.Position._
import com.seismic.utils.ValueMapHelper._

class HandleMeter(font: Font,
                  size: Dimension,
                  graphics: Graphics) extends JLayeredPane {
  setPreferredSize(size)

  var value = 0

  val centerX = size.getWidth / 2f
  val centerY = size.getHeight / 2f

  val label = new JLabel("10234")
  label.setFont(font)
  label.setForeground(new Color(200, 200, 200))
  label.setOpaque(true)

  val indicator = new MeterCircleIndicator(size)

  position(indicator).at(0, 0).in(this)
  setLayer(indicator, 1)

  val labelDimensions = font.getFontMeasurement(graphics).getFontDimensions("1024")
  val labelX = centerX.toInt - labelDimensions.getWidth / 2f
  val labelY = centerY.toInt - labelDimensions.getHeight / 2f

  position(label).at(labelX.toInt, labelY.toInt).in(this)
  setLayer(label, 2)

  def setValue(value: Int): Unit = {
    this.value = value
    indicator.setAngle(map(value, 0, 1023, 0, (2f * Math.PI).toFloat))
    label.setText(f"$value%4s")
    repaint()
  }

  override def setBackground(color: Color): Unit = {
    indicator.setBackground(color)
    label.setBackground(color)
  }

  addMouseListener(new MouseListener() {
    override def mouseExited(e: MouseEvent): Unit = {}

    override def mouseClicked(e: MouseEvent): Unit = {}

    override def mouseEntered(e: MouseEvent): Unit = {}

    override def mousePressed(e: MouseEvent): Unit = {
      indicator.setAngle((Math.random() * (2 * Math.PI)).toFloat)
      repaint()
    }

    override def mouseReleased(e: MouseEvent): Unit = {}
  })
}
