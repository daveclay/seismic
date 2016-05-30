package com.seismic.utils

import java.util.Random

object RandomHelper {
  val randomImpl = new Random

  def random = randomImpl

  def pick[T](values: T*) = {
    values(random.nextInt(values.size))
  }
}
