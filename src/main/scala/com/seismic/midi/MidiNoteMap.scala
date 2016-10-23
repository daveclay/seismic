package com.seismic.midi
import com.seismic.scala.StringExtensions._

object MidiNoteMap {

  def main(args: Array[String]): Unit = {
    println(NOTE_NAMES_TO_VALUES_MAP.keys)
  }

  /**
    * Maschine does -2 to 8
    */
  val octaveShift = -2

  val NOTE_NAMES = Array("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
  val NOTE_NAMES_TO_VALUES_MAP = 0.to(127).map { i =>
    val octave = (i / 12) + octaveShift
    f"${NOTE_NAMES(i % 12)}$octave" -> i
  }.toMap[String, Int]

  def channelForNote(note: String, defaultChannel: Int) = {
    if (note.contains("/")) {
      note.split("/")(1).toInt - 1
    } else {
      defaultChannel
    }
  }

  def midiValueForNote(note: String) = {
    valueForNote(noteWithoutSuffix(noteWithoutPrefix(note)))
  }

  def noteForMidiValue(value: Int) = {
    val octave = (value / 12) + octaveShift
    val note = value % 12
    NOTE_NAMES(note) + octave
  }

  private def valueForNote(noteName: String) = {
    if (noteName.charAt(0).isDigit) {
      noteName.toInt
    } else {
      NOTE_NAMES_TO_VALUES_MAP.get(noteName) match {
        case Some(value) => value
        case None =>
          println(s"Unknown note value '$noteName'")
          0
      }
    }
  }

  private def noteWithoutSuffix(note: String) = {
    if (note.contains("/")) {
      note.split("/")(0)
    } else {
      note
    }
  }

  private def noteWithoutPrefix(note: String) = {
    if (note.startsWithAny("N", "X", "T")) {
      note.drop(1)
    } else {
      note
    }
  }
}
