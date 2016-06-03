package com.seismic.ui.p

import processing.core.PApplet

object PAppletRunner {

  def run(pApplet: PApplet) {
    val args = Array( pApplet.getClass().getName() )
    PApplet.runSketch(args, pApplet)
  }

  def run(pApplet: PApplet, display: Int) {
    val args = Array( "--display=" + display, pApplet.getClass().getName() )
    PApplet.runSketch(args, pApplet)
  }
}
