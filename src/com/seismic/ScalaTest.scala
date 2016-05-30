package com.seismic

import com.seismic.p.PAppletRunner
import processing.core.PApplet
import themidibus.MidiBus

object ScalaTest {
  def main(args: Array[String]): Unit = {
    PAppletRunner.run(new ScalaTest)
  }
}

class ScalaTest extends PApplet {

  override def setup(): Unit = {
    val midiBus = new MidiBus(this, -1, "IAC Bus 2");

  }

  override def draw(): Unit = {
    background(color(255, 100, 0))
  }

}
