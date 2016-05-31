package com.seismic

import com.seismic.Thresholds.{highHandleThreshold, lowHandleThreshold}
import com.seismic.p.ProcessingUtils._

/**
  * TODO: rotating the handle selects the instrument. To change the number of instruments done by providing a different
  * array of instruments.
  *
  * So what does changing the threshold values do? Allow for a wider range of rotation motion.
  * Also, to change from a linear mapping of values to a logarithmic mapping requires a different implementation of
  * TriggerMap with a variety of arguments that might specify a bezier curve or something more radical. In my case,
  * random will likely be something I decide I want.
  */


case class SetList(name: String, songs: Array[Song])

case class Song(name: String,
                channel: Int,
                phrases: Array[Phrase])

case class Phrase(name: String,
                  kickInstruments: Array[Instrument],
                  snareInstruments: Array[Instrument]) {

  private def instrumentsByValue(instruments: Array[Instrument]) = {
    (value: Int) => {
      val idx = map(value, lowHandleThreshold, highHandleThreshold, 0, instruments.length - 1)
      instruments(idx)
    }
  }

  private val instrumentMapByName = Map(
    "KICK" -> instrumentsByValue(kickInstruments),
    "SNARE" -> instrumentsByValue(snareInstruments)
  )

  def instrumentFor(name: String, handleValue: Int) = {
    instrumentMapByName(name)(handleValue)
  }
}

object Thresholds {
  val lowHandleThreshold = 100
  val highHandleThreshold = 800
}
