package com.seismic.midi

import org.mockito.Mockito._
import com.seismic.test.Test

class MidiNoteMapTest extends Test {

  val midiNoteMap = MidiNoteMap

  "MidiNoteMap" should "return the lowest note value for C-2" in {
    MidiNoteMap.valueForNote("C-2") should be (0)
  }

  it should "return the C-2 for 0" in {
    MidiNoteMap.noteForValue(0) should be ("C-2")
  }

  it should "return the G8 for 127" in {
    MidiNoteMap.noteForValue(127) should be ("G8")
  }

  it should "return 127 for G8" in {
    MidiNoteMap.valueForNote("G8") should be (127)
  }

  it should "return 61 for C#3" in {
    MidiNoteMap.valueForNote("C#3") should be (61)
  }

  it should "return 1 for C#-2" in {
    MidiNoteMap.valueForNote("C#-2") should be (1)
  }
}
