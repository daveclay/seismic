package com.seismic.ui.swing

import java.awt._
import javax.swing.JPanel

class MeterCircleIndicator(size: Dimension) extends JPanel {
  setPreferredSize(size)

  var angle = 0f
  val length = size.getWidth / 2f
  val centerX = size.getWidth / 2f
  val centerY = size.getHeight / 2f

  def setAngle(angle: Float): Unit = {
    this.angle = angle
  }

  override def paint(graphics: Graphics): Unit = {
    super.paint(graphics)

    val g2d = graphics.asInstanceOf[Graphics2D]

    val startX = centerX + Math.cos(angle) * (length - 20)
    val startY = centerY + Math.sin(angle) * (length - 20)

    val pointerX = startX + Math.cos(angle) * 20
    val pointerY = startY + Math.sin(angle) * 20

    val opposite = angle + Math.PI
    val endX = centerX + Math.cos(opposite) * length
    val endY = centerY + Math.sin(opposite) * length

    g2d.setColor(getBackground)
    g2d.fillRect(0, 0, getWidth, getHeight)

    g2d.setColor(Color.BLACK)
    g2d.fillOval(0, 0, getWidth, getHeight)

    g2d.setColor(new Color(90, 90, 90))
    g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL))
    g2d.drawLine(startX.toInt, startY.toInt, centerX.toInt, centerY.toInt)
    g2d.drawLine(centerX.toInt, centerY.toInt, endX.toInt, endY.toInt)
    g2d.setColor(new Color(255, 120, 0))
    g2d.drawLine(startX.toInt, startY.toInt, pointerX.toInt, pointerY.toInt)
  }
}
