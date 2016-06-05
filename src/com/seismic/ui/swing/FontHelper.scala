package com.seismic.ui.swing

import java.awt.{Font, Graphics}

object FontHelper {
  implicit def extendFont(font: Font):ExtendedFont = new ExtendedFont(font)

  class ExtendedFont(font: Font) {
    var fontMeasurementOpt: Option[FontMeasurements] = None

    def getHeight(graphics: Graphics) = {
      getFontMeasurement(graphics).getFontHeight
    }

    def getFontMeasurement(graphics: Graphics) = {
      fontMeasurementOpt.getOrElse {
        val measurements = new FontMeasurements(font, graphics)
        fontMeasurementOpt = Some(measurements)
        measurements
      }
    }
  }
}
