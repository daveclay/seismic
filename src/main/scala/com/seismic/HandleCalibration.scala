package com.seismic

import com.seismic.utils.ValueMapHelper._

case class HandleCalibration(var calibrationMinValue: Int = 0,
                             var calibrationMaxValue: Int = 1023,
                             var inverted: Boolean = true) {

  def select[T](value: Int, items: Seq[T]) = {
    val range = calibrationMaxValue - calibrationMinValue
    val valueAdjustedForRange = Math.max(0, Math.min(calibrationMaxValue, value - calibrationMinValue))

    val min = if (inverted) range else 0
    val max = if (inverted) 0 else range

    items(Math.round(map(valueAdjustedForRange, min, max, 0, items.size - 1)))
  }
}

case class TriggerThresholds(var kickThreshold: Int = 900, var snareThreshold: Int = 900)

