package com.seismic.midi

object MidiNoteMap {

  def main(args: Array[String]): Unit = {
    println(NOTE_MAP.keys)
  }

  /**
    * Maschine does -2 to 8
    */
  val octaveShift = -2

  val NOTE_NAMES = Array("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
  val NOTE_MAP = 0.to(127).map { i =>
    val octave = (i / 12) + octaveShift
    f"${NOTE_NAMES(i % 12)}$octave" -> i
  }.toMap[String, Int]

  def noteForValue(value: Int) = {
    val octave = (value / 12) + octaveShift
    val note = value % 12
    NOTE_NAMES(note) + octave
  }

  def valueForNote(key: String) = {
    NOTE_MAP(key)
  }
}
