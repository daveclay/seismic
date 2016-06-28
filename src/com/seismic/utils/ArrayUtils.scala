package com.seismic.utils

object ArrayUtils {
  def wrapIndex(index: Int, array: Seq[Any]) = {
    if (index < 0) {
      array.size - 1
    } else if (index >= array.size) {
      0
    } else {
      index
    }
  }
}
