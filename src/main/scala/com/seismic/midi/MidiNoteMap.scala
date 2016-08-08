package com.seismic.midi
import com.seismic.scala.StringExtensions._

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

  def midiValueForNote(note: String) = {
    valueForNote(noteWithoutPrefix(note))
  }

  def noteForMidiValue(value: Int) = {
    val octave = (value / 12) + octaveShift
    val note = value % 12
    NOTE_NAMES(note) + octave
  }

  private def valueForNote(key: String) = {
    NOTE_MAP(key)
  }

  private def noteWithoutPrefix(note: String) = {
    if (note.startsWithAny("N", "X", "T")) {
      note.drop(1)
    } else {
      note
    }
  }
}
