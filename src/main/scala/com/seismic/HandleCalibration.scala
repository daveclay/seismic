package com.seismic

import com.seismic.utils.ArrayUtils.getConstrainedItem
import com.seismic.utils.ValueMapHelper._

case class HandleCalibration(var calibrationMinValue: Int = 0,
                             var calibrationMaxValue: Int = 1023,
                             var inverted: Boolean = true) {

  def select[T](value: Int, items: Seq[T]) = {
    val (min, max) = getMinMax

    val index = Math.round(map(value, min, max, 0, items.size - 1))
    getConstrainedItem(index, items)
  }

  def getMinMax = {
    val min = if (inverted) calibrationMaxValue else calibrationMinValue
    val max = if (inverted) calibrationMinValue else calibrationMaxValue
    (min, max)
  }
}

case class TriggerThreshold(threshold: Int = 900)

case class TriggerThresholds(var kickThreshold: Int = 900, var snareThreshold: Int = 900)

