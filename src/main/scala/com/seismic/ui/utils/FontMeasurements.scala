package com.seismic.ui.utils

import java.awt.{Dimension, Font, Graphics}

class FontMeasurements(font: Font, graphics: Graphics) {
  def getFontHeight = {
    val metrics = graphics.getFontMetrics(font)
    metrics.getHeight
  }

  def getFontDimensions(text: String) = {
    val metrics = graphics.getFontMetrics(font)
    val h = metrics.getHeight
    val w = metrics.stringWidth(text)
    new Dimension(w, h)
  }
}
