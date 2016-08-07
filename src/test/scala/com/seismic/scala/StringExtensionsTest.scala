package com.seismic.scala

import org.scalatest.{FlatSpec, Matchers}

import com.seismic.scala.StringExtensions._

class StringExtensionsTest extends FlatSpec with Matchers {
  val s = "ABCD"

  "The string ABCD" should "startsWithAny X, T, or A" in {
    s.startsWithAny("X", "T", "A") should be (true)
  }

  it should "not startsWithAny X, T, or Q" in {
    s.startsWithAny("X", "T", "Q") should be (false)
  }

}
