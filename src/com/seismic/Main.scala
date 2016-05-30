package com.seismic

import com.seismic.serial.SeismicSerial
import com.seismic.serial.SeismicSerial.SerialListener

object Main {

  def main(args: Array[String]) {
    SeismicSerial.list() foreach { name =>
      println(name)
    }

    val serial = new SeismicSerial(new SerialListener {
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


