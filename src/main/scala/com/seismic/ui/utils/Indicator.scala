package com.seismic.ui.utils

import java.awt.{Color, Dimension}
import javax.swing.JPanel

import com.daveclay.swing.color.GradientValueMap
import com.daveclay.swing.util.Position._
import com.seismic.ui.utils.layout.GridBagLayoutHelper
import com.seismic.utils.ValueMapHelper._

class Indicator(size: Dimension,
                minValue: Int = 0,
                maxValue: Int = 1023) extends JPanel {
  val meterColorMap = new GradientValueMap()
  meterColorMap.addRedPoint(0, 100)
  meterColorMap.addGreenPoint(0, 100)
  meterColorMap.addBluePoint(0, 100)

  meterColorMap.addRedPoint(400, 250)
  meterColorMap.addGreenPoint(400, 60)
  meterColorMap.addBluePoint(400, 0)

  meterColorMap.addRedPoint(600, 250)
  meterColorMap.addGreenPoint(600, 180)
  meterColorMap.addBluePoint(600, 30)

  meterColorMap.addRedPoint(800, 200)
  meterColorMap.addGreenPoint(800, 250)
  meterColorMap.addBluePoint(800, 0)

  meterColorMap.addRedPoint(1000, 0)
  meterColorMap.addGreenPoint(1000, 255)
  meterColorMap.addBluePoint(1000, 0)

  setPreferredSize(size)
  setMinimumSize(size)

  private val lightDimension = new Dimension(1, size.height)
  
  val light = new JPanel()
  light.setBackground(Color.BLACK)
  light.setPreferredSize(lightDimension)
  light.setMinimumSize(lightDimension)

  position(light).at(0, 0).in(this)

  def setValue(value: Int): Unit = {
    val size: Dimension = getSize
    val width = map(value, minValue, maxValue, 0, size.getWidth.toFloat).toInt
    light.setSize(new Dimension(width, Math.ceil(size.getHeight).toInt))
    light.setBackground(meterColorMap.getColorForValue(value))
  }

  /*
  addComponentListener(new ComponentAdapter() {
    override def componentResized(e: ComponentEvent): Unit = {
    }
  })
  */
}

object MeterColors {
  val redOrangeGreen = new GradientValueMap()
  redOrangeGreen.addRedPoint(0, 180)
  redOrangeGreen.addGreenPoint(0, 0)
  redOrangeGreen.addBluePoint(0, 0)

  redOrangeGreen.addRedPoint(200, 250)
  redOrangeGreen.addGreenPoint(200, 60)
  redOrangeGreen.addBluePoint(200, 0)

  redOrangeGreen.addRedPoint(600, 250)
  redOrangeGreen.addGreenPoint(600, 180)
  redOrangeGreen.addBluePoint(600, 30)

  redOrangeGreen.addRedPoint(800, 200)
  redOrangeGreen.addGreenPoint(800, 250)
  redOrangeGreen.addBluePoint(800, 0)

  redOrangeGreen.addRedPoint(1000, 0)
  redOrangeGreen.addGreenPoint(1000, 255)
  redOrangeGreen.addBluePoint(1000, 0)

}
