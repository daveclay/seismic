package com.seismic.scala

object ArrayExtensions {
  implicit class HolyFuckAnArrayWithARemoveMethodThatIHadToFuckingWriteMyself[T](array: Array[T]) {
    def remove(itemToRemove: T) = {
      array.filterNot { item => item.equals(itemToRemove) }
    }
  }
}
