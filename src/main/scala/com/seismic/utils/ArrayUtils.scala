package com.seismic.utils

object ArrayUtils {
  def getConstrainedItem[T](index: Int, array: Seq[T]): T = {
    if (index >= array.size) {
      array.last
    } else if (index < 0) {
      array.head
    } else {
      array(index)
    }
  }

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
