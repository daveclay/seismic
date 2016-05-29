package com.seismic

import com.seismic.serial.Serial
import com.seismic.serial.Serial.SerialListener
import processing.core.PApplet

object Main {

  def main(args: Array[String]) {
    Serial.list() foreach { name =>
      println(name)
    }

    val serial = new Serial(new SerialListener {
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

class PAppletRunner(papplet: PApplet) extends Runnable {

  def run: Unit = {

  }
}
