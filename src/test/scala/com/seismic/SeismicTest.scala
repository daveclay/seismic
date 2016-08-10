package com.seismic

import com.seismic.io.Preferences
import com.seismic.messages.TriggerOnMessage
import com.seismic.midi.MIDIIO
import com.seismic.test.Test
import org.mockito.Mockito._
import org.mockito.Matchers._

class SeismicTest extends Test {

  "When triggering notes" - {
    "should trigger a midi note for the first kick instrument" in new SongData {
      seismic.trigger(TriggerOnMessage("KICK", 1023, 0))
      verify(midiIO).sendNoteOn(0, 61, 127)
    }

    "should trigger a midi note for the second kick instrument" in new SongData {
      seismic.trigger(TriggerOnMessage("KICK", 1023, 1023))
      verify(midiIO).sendNoteOn(0, 5, 127)
    }

    "should trigger a midi note for the first snare instrument" in new SongData {
      seismic.trigger(TriggerOnMessage("SNARE", 1023, 0))
      verify(midiIO).sendNoteOn(0, 1, 127)
    }

    "should trigger a midi note for the second snare instrument" in new SongData {
      seismic.trigger(TriggerOnMessage("SNARE", 1023, 1023))
      verify(midiIO).sendNoteOn(0, 127, 127)
    }

    "for notes prefixed with N" - {
      "it should trigger a midi note off when trigger() is called" in new SongData {
        val instrument = phrase.addNewKickInstrument()
        instrument.setNotes(Array("NC#3"))

        seismic.trigger(TriggerOnMessage("KICK", 1023, 1023))
        verify(midiIO).sendNoteOff(0, 61, 0)
      }

      "it should NOT trigger a midi note off when off() is called" in new SongData {
        val instrument = phrase.addNewKickInstrument()
        instrument.setNotes(Array("NC#3"))
        triggeredState.triggered("KICK", instrument, song)

        seismic.off("KICK")
        verify(midiIO, never()).sendNoteOff(anyInt(), anyInt(), anyInt())
      }
    }

    "for notes prefixed with T" - {
      "it should NOT trigger a midi note on when trigger() is called" in new SongData {
        val instrument = phrase.addNewKickInstrument()
        instrument.setNotes(Array("TC#3"))

        seismic.trigger(TriggerOnMessage("KICK", 1023, 1023))
        verify(midiIO, never()).sendNoteOn(anyInt(), anyInt(), anyInt())
      }

      "it should NOT trigger a midi note off when trigger() is called" in new SongData {
        val instrument = phrase.addNewKickInstrument()
        instrument.setNotes(Array("TC#3"))

        seismic.trigger(TriggerOnMessage("KICK", 1023, 1023))
        verify(midiIO, never()).sendNoteOff(anyInt(), anyInt(), anyInt())
      }

      "it should trigger a midi note off when off() is called" in new SongData {
        val instrument = phrase.addNewKickInstrument()
        instrument.setNotes(Array("TC#3"))
        triggeredState.triggered("KICK", instrument, song)

        seismic.off("KICK")
        verify(midiIO).sendNoteOff(0, 61, 0)
      }
    }

    "for notes prefixed with X" - {
      "it should trigger a midi note on when trigger() is called" in new SongData {
        val instrument = phrase.addNewKickInstrument()
        instrument.setNotes(Array("XC#3"))

        seismic.trigger(TriggerOnMessage("KICK", 1023, 1023))
        verify(midiIO).sendNoteOn(0, 61, 127)
      }

      "it should NOT trigger a midi note off when off() is called" in new SongData {
        val instrument = phrase.addNewKickInstrument()
        instrument.setNotes(Array("XC#3"))
        triggeredState.triggered("KICK", instrument, song)

        seismic.off("KICK")
        verify(midiIO, never()).sendNoteOff(anyInt(), anyInt(), anyInt())
      }
    }
  }

  "when adding instruments" - {
    "if a prefixed note exists" - {
      "it should not break" in new SongData {
        val instrument = phrase.addNewKickInstrument()
        instrument.setNotes(Array("NG3"))

        phrase.addNewKickInstrument()
      }
    }
  }

  "when adding phrase" - {
    "it should not break" in new SongData {
      song.addPhrase()
    }
  }

  trait SongData {

    val midiIO = mock[MIDIIO]
    val preferences = Preferences(".")
    val triggeredState = new TriggeredState
    val seismic = new Seismic(midiIO, preferences, triggeredState)

    val kickInstrument1 = Instrument(Array("C#3"))
    val kickInstrument2 = Instrument(Array("F-2"))
    val snareInstrument1 = Instrument(Array("C#-2"))
    val snareInstrument2 = Instrument(Array("G8"))

    val phrase = Phrase("Test Phrase", 1)
    phrase.setKickInstruments(Array(kickInstrument1, kickInstrument2))
    phrase.setSnareInstruments(Array(snareInstrument1, snareInstrument2))

    val song = Song("Test Song", 1)
    song.setPhrases(Array(phrase))

    seismic.setCurrentSong(song)
    seismic.setCurrentPhrase(phrase)
  }
}
