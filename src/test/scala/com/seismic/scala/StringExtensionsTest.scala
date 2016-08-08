package com.seismic.scala

import com.seismic.scala.StringExtensions._
import com.seismic.test.Test

class StringExtensionsTest extends Test {
  val s = "ABCD"

  "The string ABCD should startsWithAny X, T, or A" in {
    s.startsWithAny("X", "T", "A") should be (true)
  }

  "it should not startsWithAny X, T, or Q" in {
    s.startsWithAny("X", "T", "Q") should be (false)
  }
}
