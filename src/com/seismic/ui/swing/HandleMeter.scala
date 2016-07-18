package com.seismic.ui.swing

import java.awt.event.{MouseAdapter, MouseEvent, MouseListener}
import java.awt.{Color, Dimension}
import javax.swing.{JLabel, JLayeredPane}

import com.daveclay.swing.util.Position._
import com.seismic.io.Preferences.getPreferences
import com.seismic.utils.ValueMapHelper._

class HandleMeter(size: Dimension) extends JLayeredPane {
  setPreferredSize(size)

  private val indicatorSize = new Dimension(size.height, size.height)
  private val centerX = indicatorSize.getWidth / 2f
  private val centerY = indicatorSize.getHeight / 2f

  private var lastValue = 0

  private val label = SwingComponents.label("----", SwingComponents.monoFont11)
  label.setOpaque(true)

  private val preferences = getPreferences
  private val calibration = preferences.handleCalibration
  private val onMinSet = (s: String) => {
    calibration.calibrationMinValue = s.toInt
    preferences.save()
  }

  private val onMaxSet = (s: String) => {
    calibration.calibrationMaxValue = s.toInt
    preferences.save()
  }

  private val minField = new LabeledTextField("min", 4, LabeledTextField.Vertical, 0, onMinSet)
  minField.setText(calibration.calibrationMinValue.toString)

  private val maxField = new LabeledTextField("max", 4, LabeledTextField.Vertical, 0, onMaxSet)
  maxField.setText(calibration.calibrationMaxValue.toString)

  private val indicator = new MeterCircleIndicator(indicatorSize)

  position(indicator).at(0, 0).in(this)
  setLayer(indicator, 1)

  private val labelDimensions = new Dimension(36, 18)
  private val labelX = centerX.toInt - labelDimensions.getWidth / 2f + 2 // I don't know, nudging...
  private val labelY = centerY.toInt - labelDimensions.getHeight / 2f + 2

  position(label).at(labelX.toInt, labelY.toInt).in(this)
  setLayer(label, 2)

  position(minField).toTheRightOf(indicator).withMargin(4).in(this)
  position(maxField).below(minField).withMargin(4).in(this)

  def setValue(value: Int): Unit = {
    this.lastValue = value
    // TODO: calibrate!
    /*
    If the lastValue sent was 512, and I click the meter, that means 512 should be 0, indicating the first sample should be triggered at 512.
    instead of the raw value being 0 to 1023, it should be 512 to the next rotation angle.
     */

    indicator.setAngle(map(value, 0, 1023, 0, (2f * Math.PI).toFloat))
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

  addMouseListener(new MouseAdapter() {
    override def mousePressed(e: MouseEvent): Unit = {
    }
  })
}
