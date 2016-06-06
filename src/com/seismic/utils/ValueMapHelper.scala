package com.seismic.utils

object ValueMapHelper {

  def map(value: Float, start1: Float, stop1: Float, start2: Float, stop2: Float) = {
    start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1))
  }
}
