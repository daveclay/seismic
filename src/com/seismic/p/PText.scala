package com.seismic.p

import processing.core.PApplet

case class PText(location: Location) {
  var text: Option[String] = None

  def set(text: String): Unit = {
    this.text = Option(text)
  }

  def render(canvas: PApplet): Unit = {
    text match {
      case Some(text) =>
        canvas.text(text, location.x, location.y)
      case _ =>
    }
  }
}
