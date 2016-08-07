package com.seismic.scala

import com.seismic.Phrase

object OptionExtensions {

  implicit def extendOption[T](option: Option[T]):ExtendedOption[T] = new ExtendedOption(option)

  class ExtendedOption[T](option: Option[T]) {

    def tap(f: (T => Unit)) = {
      option.foreach { t => f(t) }
      option
    }

    def getOrCreateIfNone(builder: () => T, setter: (Option[T] => Unit)) = {
      option match {
        case Some(v) => v
        case None =>
          val v = builder()
          setter(Some(v))
          v
      }
    }
  }

}
