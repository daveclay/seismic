package com.seismic

import com.seismic.utils.ValueMapHelper._

case class HandleCalibration(var calibrationMinValue: Int = 0,
                             var calibrationMaxValue: Int = 1023) {

  def select[T](value: Int, items: Seq[T]) = {
    val min = Math.min(calibrationMaxValue, calibrationMinValue)
    val max = Math.max(calibrationMaxValue, calibrationMinValue)

    val range = max - min

    val constrained = Math.min(range, Math.max(0, value))
    items(Math.round(map(constrained, range, 0, 0, items.size - 1)))
  }
}

case class HandleMeterCalibration(var valueAt270: Int = 0,
                                  var valueAt180: Int = 1023)

case class TriggerThresholds(var kickThreshold: Int = 900, var snareThreshold: Int = 900)

