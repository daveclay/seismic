package com.seismic.midi

object MidiNoteMap {

  def main(args: Array[String]): Unit = {
    println(MidiNoteMap.valueForNote("C3"))
    println(MidiNoteMap.valueForNote("C2"))
    println(MidiNoteMap.valueForNote("C0"))
    println(MidiNoteMap.valueForNote("F#8"))
    println(MidiNoteMap.valueForNote("120"))
    println(MidiNoteMap.valueForNote("3"))
  }

  val NOTE_NAMES = Array("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
  val NOTE_MAP = 0.to(127).map { i =>
    f"${NOTE_NAMES(i % 12)}${i / 12}" -> i
  }.toMap[String, Int]

  def noteForValue(value: Int) = {
    val octave = (value / 12) - 1
    val note = value % 12
    NOTE_NAMES(note) + octave
  }

  def valueForNote(key: String) = {
    key.forall(Character.isDigit) match {
      case true => key.toInt
      case false => NOTE_MAP(key)
    }
  }
}
