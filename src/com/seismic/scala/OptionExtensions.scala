package com.seismic.scala

object OptionExtensions {

  implicit def extendOption[T](option: Option[T]):ExtendedOption[T] = new ExtendedOption(option)

  class ExtendedOption[T](option: Option[T]) {

    def doOrElse[R](f: (T) => R) = {
      option match {
        case Some(v) => f(v)
        case None =>
      }
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
