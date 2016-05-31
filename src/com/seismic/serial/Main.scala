package com.seismic.serial

object Main {

  def main(args: Array[String]) {
    StandardSerialIO.list() foreach { name =>
      println(name)
    }

    val serial = new StandardSerialIO(args(0))
    serial.open(new SerialListener {
      def handleMessage(bytes: Array[Byte]): Unit = {
        val result = bytes.foldLeft(0) { (value, byte) =>
          (value << 8) + (byte & 0xff)
        }

        println(result & 0xfff)
      }

      override def dataAvailable(): Unit = {

      }
    })

    Thread.sleep(2000)
    serial.write("A")
  }
}


