package com.seismic.scala

object StringExtensions {

  implicit def starstWithAnyString(s: String): StartsWithAnyString = new StartsWithAnyString(s)

  class StartsWithAnyString(val s: String) {
    def startsWithAny(prefixes: String*) = {
      !prefixes.forall { prefix => !s.startsWith(prefix) }
    }
  }

}
