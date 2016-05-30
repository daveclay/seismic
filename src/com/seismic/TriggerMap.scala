package com.seismic

import com.seismic.p.ProcessingUtils._

class TriggerMap[T](from: Int, to: Int, instruments: Array[T]) {
  def mapValue(handleValue: Int) = {
    val idx = map(handleValue, from, to, 0, instruments.length - 1)
    instruments(idx)
  }
}
