package com.seismic.messages

import com.seismic.utils.RandomHelper._

class MessageSource {

  def nextMessage = {
    val s = new StringBuilder
    s.append(pick("ON", "OFF"))
    s.append(",")
    s.append(pick("KICK", "SNARE"))
    s.append(",")
    s.append(random.nextInt(1023))
    s.append(",")
    s.append(random.nextInt(1023))
    s.toString
  }
}
