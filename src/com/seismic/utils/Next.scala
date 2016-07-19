package com.seismic.utils

object Next {
  def highest[T](items: Seq[T], getter: (T) => Int, initial: Int = 0) = {
    items.foldLeft(initial) { (next, item) =>
      val itemValue = getter(item)
      if (itemValue > next) {
        itemValue
      } else {
        next
      }
    }
  }

  def next[T](items: Seq[T], getter: (T) => Int, initial: Int = 0) = {
    highest(items, getter, initial) + 1
  }
}
