package com.seismic.serial

import SerialListener

object Main {

  def main(args: Array[String]) {
    SerialIO.list() foreach { name =>
      println(name)
    }

    val serial = new SerialIO(new SerialListener {
      override def handleMessage(bytes: Array[Byte]): Unit = {
        val result = bytes.foldLeft(0) { (value, byte) =>
          (value << 8) + (byte & 0xff)
        }

        println(result & 0xfff)
      }
    }, args(0))

    Thread.sleep(2000)
    serial.write("A")
  }
}


