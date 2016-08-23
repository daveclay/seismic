package com.seismic

import com.seismic.test.Test

class HandleCalibrationTest extends Test {

  "with default min and max" - {
    "when inverted" - {
      "should map 800 to A" in new TestData {
        handleCalibration.select(800, Array("A", "B")) should be ("A")
      }

      "should map 100 to B" in new TestData {
        handleCalibration.select(100, Array("A", "B")) should be ("B")
      }
    }

    "when not inverted" - {
      "should map 800 to B" in new TestData {
        handleCalibration.inverted = false
        handleCalibration.select(800, Array("A", "B")) should be ("B")
      }

      "should map 100 to A" in new TestData {
        handleCalibration.inverted = false
        handleCalibration.select(100, Array("A", "B")) should be ("A")
      }
    }

    "with limited calibration value ranges" - {
      "should map 800 to B" in new TestData {
        handleCalibration.calibrationMinValue = 800
        handleCalibration.calibrationMaxValue = 900

        handleCalibration.select(800, Array("A", "B")) should be ("B")
      }

      "should map 870 to A" in new TestData {
        handleCalibration.calibrationMinValue = 800
        handleCalibration.calibrationMaxValue = 900

        handleCalibration.select(870, Array("A", "B")) should be ("A")
      }
    }
  }

  trait TestData {
    val handleCalibration = HandleCalibration(0, 1023)
  }
}
