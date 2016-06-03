package com.seismic.ui.p

import com.daveclay.swing.color.GradientValueMap
import controlP5.{ControlP5, Knob}
import processing.core.PApplet

object MeterFactory {

  var id = 0

  def buildMeter(location: Location, canvas: PApplet) = {
    id += 1
    new Meter(f"meter_$id", location, canvas)
  }
}

object MeterColors {
  val meterColorMap = new GradientValueMap()
  meterColorMap.addRedPoint(0, 30)
  meterColorMap.addGreenPoint(0, 30)
  meterColorMap.addBluePoint(0, 30)

  meterColorMap.addRedPoint(200, 90)
  meterColorMap.addGreenPoint(200, 30)
  meterColorMap.addBluePoint(200, 20)

  meterColorMap.addRedPoint(400, 160)
  meterColorMap.addGreenPoint(400, 70)
  meterColorMap.addBluePoint(400, 10)

  meterColorMap.addRedPoint(800, 200)
  meterColorMap.addGreenPoint(800, 100)
  meterColorMap.addBluePoint(800, 0)

  meterColorMap.addRedPoint(1000, 255)
  meterColorMap.addGreenPoint(1000, 255)
  meterColorMap.addBluePoint(1000, 0)

}

/**
  * A level meter
 *
  * @param location
  * @param canvas
  */
class Meter(id: String,
            location: Location,
            canvas: PApplet) {

  implicit def extendOption[T](option: Option[T]):ExtendedOption[T] = new ExtendedOption(option)

  class ExtendedOption[T](option: Option[T]) {

    def doOrIgnore[R](f: (T) => R) = {
      option match {
        case Some(v) => f(v)
        case None =>
      }
    }
  }

  var knob: Option[Knob] = None
  var lighted = false
  var fadeLength = 0
  var triggerOnValue = 0
  var fadeValue = 0
  val fadeMap = new GradientValueMap()

  def setup(controlP5: ControlP5): Unit = {
    knob = Option(controlP5.addKnob(f"$id-color")
                  .setLabelVisible(false)
                  .setRange(0, 1023) // TODO: the range is reference in two disparate locations!
                  .setShowAngleRange(false)
                  .setDecimalPrecision(0)
                  .setColorBackground(controlP5.papplet.color(30))
                  .setPosition(location.x, location.y)
                  .setViewStyle(3)
           )
  }

  def set(value: Int): Unit = {
    knob.doOrIgnore { (knob: Knob) =>
      lighted = true
      fadeValue = 0
      triggerOnValue = value

      val color = MeterColors.meterColorMap.getColorForValue(value)
      knob.setValue(value)
        .setColorForeground(color.getRGB)

      fadeMap.reset()

      fadeMap.addRedPoint(0f, 30)
      fadeMap.addGreenPoint(0f, 30)
      fadeMap.addBluePoint(0f, 30)

      fadeMap.addRedPoint(triggerOnValue.toFloat, color.getRed)
      fadeMap.addGreenPoint(triggerOnValue.toFloat, color.getGreen)
      fadeMap.addBluePoint(triggerOnValue.toFloat, color.getBlue)
    }
  }

  def off(): Unit = {
    fadeValue = triggerOnValue
  }

  def render(canvas: PApplet): Unit = {
    if (lighted && fadeValue > 0) {
      fadeValue = PApplet.max(fadeValue - (triggerOnValue / 10), 0)
      knob.doOrIgnore { (knob: Knob) =>
        if (fadeValue > -1) {
          val color = fadeMap.getColorForValue(fadeValue)
          knob.setValue(fadeValue)
          .setColorForeground(color.getRGB)
        }
      }

      if (fadeValue == 0) {
        lighted = false
      }
    }

    knob.doOrIgnore { (knob: Knob) =>

      val angle = knob.getAngle

      val length = knob.getRadius - 3
      val centerX = location.x.toFloat + knob.getWidth / 2f
      val centerY = location.y.toFloat + knob.getHeight / 2f
      val endX = centerX + Math.cos(angle) * length
      val endY = centerY + Math.sin(angle) * length

      canvas.stroke(180)
      canvas.line(centerX,
                   centerY,
                   endX.toFloat,
                   endY.toFloat)

      val centerSize = length + 4
      canvas.noStroke()
      canvas.fill(50, 50, 60)
      canvas.ellipse(centerX, centerY, centerSize, centerSize)
    }
  }
}
