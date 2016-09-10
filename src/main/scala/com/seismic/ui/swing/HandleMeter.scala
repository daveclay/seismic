package com.seismic.ui.swing

import java.awt.event.{MouseAdapter, MouseEvent, MouseListener}
import java.awt.{Color, Dimension}
import javax.swing.{BorderFactory, JLabel, JLayeredPane, JPanel}

import com.daveclay.swing.util.Position._
import com.seismic.io.Preferences.getPreferences
import com.seismic.utils.ValueMapHelper._

object Angles {
  def main(args: Array[String]): Unit = {
    for (i <- 0 to 1023) {
      val radians = map(i, 0, 1023, 0, (2f * Math.PI).toFloat)
      println(f"$i -> $radians or ${Math.toDegrees(radians)}")
    }

    // 90º is 1.5708f)
    println(map(Math.toRadians(180).toFloat, 0, (2f * Math.PI).toFloat, 0, 1023))
  }
}

class HandleMeter(size: Dimension) extends JLayeredPane {
  setPreferredSize(size)

  private val indicatorSize = new Dimension(size.height, size.height)
  private val centerX = indicatorSize.getWidth / 2f
  private val centerY = indicatorSize.getHeight / 2f

  private var lastRawValue = 0
  private var calibrateWhich = true

  private val label = SwingComponents.label("----", SwingComponents.monoFont11)
  label.setOpaque(true)

  private val preferences = getPreferences
  private val handleCalibration = preferences.handleCalibration

  private val onMinSet = (s: String) => {
    handleCalibration.calibrationMinValue = s.toInt
    preferences.save()
  }

  private val onMaxSet = (s: String) => {
    handleCalibration.calibrationMaxValue = s.toInt
    preferences.save()
  }

  private val minField = new LabeledTextField("min", 4, LabeledTextField.Vertical, 0, onMinSet)
  minField.setText(handleCalibration.calibrationMinValue.toString)

  private val maxField = new LabeledTextField("max", 4, LabeledTextField.Vertical, 0, onMaxSet)
  maxField.setText(handleCalibration.calibrationMaxValue.toString)

  private val invertLabel = SwingComponents.label("INV")
  private val invertHandleToggle = new JPanel()
  invertHandleToggle.setPreferredSize(new Dimension(12, 12))
  updateReverseHandleToggle()
  invertHandleToggle.setBorder(BorderFactory.createLineBorder(Color.GRAY))
  invertHandleToggle.addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent): Unit = toggleReverseHandle()
  })

  private val indicator = new MeterCircleIndicator(indicatorSize)
  indicator.addMouseListener(new MouseAdapter() {
    override def mousePressed(e: MouseEvent): Unit = calibrate()
  })

  position(indicator).at(0, 0).in(this)
  setLayer(indicator, 1)

  private val labelDimensions = new Dimension(36, 18)
  private val labelX = centerX.toInt - labelDimensions.getWidth / 2f + 2 // I don't know, nudging...
  private val labelY = centerY.toInt - labelDimensions.getHeight / 2f + 2

  position(label).at(labelX.toInt, labelY.toInt).in(this)
  setLayer(label, 2)

  position(minField).toTheRightOf(indicator).withMargin(4).in(this)
  position(maxField).below(minField).withMargin(4).in(this)
  position(invertLabel).toTheRightOf(minField).withMargin(4).in(this)
  position(invertHandleToggle).below(invertLabel).withMargin(4).in(this)

  def setRawValue(value: Int): Unit = {
    this.lastRawValue = value

    val radians = map(value,
                       handleCalibration.calibrationMinValue,
                       handleCalibration.calibrationMaxValue,
                       Math.PI.toFloat,
                       4.71239f)

    indicator.setAngle(radians)
    label.setText(f"$value%4s")
    repaint()
  }

  def setRandomAngle(): Unit = {
    indicator.setAngle((Math.random() * (2 * Math.PI)).toFloat)
  }

  override def setBackground(color: Color): Unit = {
    indicator.setBackground(color)
    label.setBackground(color)
  }

  private def radiansForValue(value: Int) = {
    map(value, 0, 1023, 0, (2f * Math.PI).toFloat)
  }

  private def calibrate() = {
    if (calibrateWhich) {
      calibrate270()
    } else {
      calibrate180()
    }
    calibrateWhich = !calibrateWhich
  }

  private def calibrate270() {
    // TODO: highlight which field was set
    maxField.setText(lastRawValue.toString)
    onMaxSet(lastRawValue.toString)
    wasCalibrated()
  }

  private def calibrate180() {
    minField.setText(lastRawValue.toString)
    onMinSet(lastRawValue.toString)
    wasCalibrated()
  }

  private def wasCalibrated(): Unit = {
    preferences.save()
    setRawValue(lastRawValue)
  }

  private def toggleReverseHandle(): Unit = {
    handleCalibration.inverted = !handleCalibration.inverted
    preferences.save()
    updateReverseHandleToggle()
  }

  private def updateReverseHandleToggle(): Unit = {
    val color = if (handleCalibration.inverted) SwingComponents.backgroundColor else SwingComponents.orangeColor
    invertHandleToggle.setBackground(color)
    invertLabel.setForeground(color)

  }
}
