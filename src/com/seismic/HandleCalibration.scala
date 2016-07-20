package com.seismic

import com.seismic.utils.ValueMapHelper._

case class HandleCalibration(var calibrationMinValue: Int = 0,
                             var calibrationMaxValue: Int = 1023) {

  def select[T](value: Int, items: Seq[T]) = {
    val calibratedValue = map(value, 0, 1023, calibrationMinValue, calibrationMaxValue)
    val constrained = Math.min(calibrationMaxValue, Math.max(calibrationMinValue, calibratedValue))
    items(Math.round(map(constrained, calibrationMinValue, calibrationMaxValue, 0, items.size - 1)))
  }
}

case class HandleMeterCalibration(var valueAt270: Int = 1023,
                                  var valueAt180: Int = 0)

case class TriggerThresholds(var kickThreshold: Int = 900, var snareThreshold: Int = 900)

