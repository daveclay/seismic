package com.seismic

import java.io.{File, StringWriter}

import com.seismic.io.{ObjectMapperFactory, Preferences}
import com.seismic.messages.TriggerOnMessage
import com.seismic.midi.MIDIIO
import com.seismic.test.Test
import org.mockito.Mockito._
import org.mockito.Matchers._

class SeismicTest extends Test {

  "The setlist json file" - {
    "should deserialize" in {
      val in = new File("src/test/resources/New Set List Test.json")
      val setList = ObjectMapperFactory.objectMapper.readValue(in, classOf[SetList])

      setList.songs.length should be (1)
      setList.songs(0).getPhrases.length should be (2)
      setList.songs(0).getPhrases(0).getInstrumentBanks.length should be (4)
      setList.songs(0).getPhrases(0).getInstrumentBankNamed("KICK").getInstruments.length should be (6)
      setList.songs(0).getPhrases(0).getInstrumentBankNamed("ALTKICK").getInstruments.length should be (0)
      setList.songs(0).getPhrases(0).getInstrumentBankNamed("SNARE").getInstruments.length should be (4)
    }

    "should serialize" in new SongData {
      val writer = new StringWriter()
      ObjectMapperFactory.objectMapper.writeValue(writer, setList)
      val serializedSetList = ObjectMapperFactory.objectMapper.readValue(writer.toString, classOf[SetList])

      serializedSetList should be eq setList
    }
  }

  "When triggering notes" - {
    "should trigger a midi note for the first kick instrument" in new SongData {
      seismic.trigger(TriggerOnMessage("KICK", 1023, 0, false))
      verify(midiIO).sendNoteOn(0, 61, 127)
    }

    "should trigger a midi note for the second kick instrument" in new SongData {
      seismic.trigger(TriggerOnMessage("KICK", 1023, 1023, false))
      verify(midiIO).sendNoteOn(0, 5, 127)
    }

    "should trigger a midi note for the first snare instrument" in new SongData {
      seismic.trigger(TriggerOnMessage("SNARE", 1023, 0, false))
      verify(midiIO).sendNoteOn(0, 1, 127)
    }

    "should trigger a midi note for the second snare instrument" in new SongData {
      seismic.trigger(TriggerOnMessage("SNARE", 1023, 1023, false))
      verify(midiIO).sendNoteOn(0, 127, 127)
    }

    "for notes prefixed with N" - {
      "it should trigger a midi note off when trigger() is called" in new SongData {
        val instrument = phrase.getInstrumentBankNamed("KICK").addNewInstrument()
        instrument.setNotes(Array("NC#3"))

        seismic.trigger(TriggerOnMessage("KICK", 1023, 1023, false))
        verify(midiIO).sendNoteOff(0, 61, 0)
      }

      "it should NOT trigger a midi note off when off() is called" in new SongData {
        val instrument = phrase.getInstrumentBankNamed("KICK").addNewInstrument()
        instrument.setNotes(Array("NC#3"))
        triggeredState.triggered("KICK", instrument, song)

        seismic.off("KICK")
        verify(midiIO, never()).sendNoteOff(anyInt(), anyInt(), anyInt())
      }
    }

    "for notes prefixed with T" - {
      "it should NOT trigger a midi note on when trigger() is called" in new SongData {
        val instrument = phrase.getInstrumentBankNamed("KICK").addNewInstrument()
        instrument.setNotes(Array("TC#3"))

        seismic.trigger(TriggerOnMessage("KICK", 1023, 1023, false))
        verify(midiIO, never()).sendNoteOn(anyInt(), anyInt(), anyInt())
      }

      "it should NOT trigger a midi note off when trigger() is called" in new SongData {
        val instrument = phrase.getInstrumentBankNamed("KICK").addNewInstrument()
        instrument.setNotes(Array("TC#3"))

        seismic.trigger(TriggerOnMessage("KICK", 1023, 1023, false))
        verify(midiIO, never()).sendNoteOff(anyInt(), anyInt(), anyInt())
      }

      "it should trigger a midi note off when off() is called" in new SongData {
        val instrument = phrase.getInstrumentBankNamed("KICK").addNewInstrument()
        instrument.setNotes(Array("TC#3"))
        triggeredState.triggered("KICK", instrument, song)

        seismic.off("KICK")
        verify(midiIO).sendNoteOff(0, 61, 0)
      }
    }

    "for notes prefixed with X" - {
      "it should trigger a midi note on when trigger() is called" in new SongData {
        val instrument = phrase.getInstrumentBankNamed("KICK").addNewInstrument()
        instrument.setNotes(Array("XC#3"))

        seismic.trigger(TriggerOnMessage("KICK", 1023, 1023, false))
        verify(midiIO).sendNoteOn(0, 61, 127)
      }

      "it should NOT trigger a midi note off when off() is called" in new SongData {
        val instrument = phrase.getInstrumentBankNamed("KICK").addNewInstrument()
        instrument.setNotes(Array("XC#3"))
        triggeredState.triggered("KICK", instrument, song)

        seismic.off("KICK")
        verify(midiIO, never()).sendNoteOff(anyInt(), anyInt(), anyInt())
      }
    }

    "for notes suffixed with / for midi channel " - {
      "it should trigger a midi note on with the channel" in new SongData {
        val instrument = phrase.getInstrumentBankNamed("KICK").addNewInstrument()
        instrument.setNotes(Array("C#3/4"))

        seismic.trigger(TriggerOnMessage("KICK", 1023, 1023, false))
        verify(midiIO).sendNoteOn(3, 61, 127)
      }
    }

    "it should send numeric midi values" in new SongData {
      val instrument = phrase.getInstrumentBankNamed("KICK").addNewInstrument()
      instrument.setNotes(Array("6"))

      seismic.trigger(TriggerOnMessage("KICK", 1023, 1023, false))
      verify(midiIO).sendNoteOn(0, 6, 127)
    }

    "when an error occurs" in new SongData {
      val instrument = phrase.getInstrumentBankNamed("KICK").addNewInstrument()
      instrument.setNotes(Array(";"))

      seismic.trigger(TriggerOnMessage("KICK", 1023, 1023, false))
    }
  }

  "when adding instruments" - {
    "if a prefixed note exists" - {
      "it should not break" in new SongData {
        val instrument = phrase.getInstrumentBankNamed("KICK").addNewInstrument()
        instrument.setNotes(Array("NG3"))

        phrase.getInstrumentBankNamed("KICK").addNewInstrument()
      }
    }
  }

  "when adding phrase" - {
    "it should not break" in new SongData {
      song.addPhrase()
    }
  }

  "when adding song" - {
    "it should not break" in new SongData {
      setList.addSong()
    }
  }

  "when calibrating the handle, triggered midi notes" - {
    "should fire the first instrument when low" in new SongData {
      seismic.trigger(TriggerOnMessage("KICK", 800, 0, false))
      verify(midiIO).sendNoteOn(0, 61, 112)
    }

    "should fire the second instrument when high" in new SongData {
      seismic.trigger(TriggerOnMessage("KICK", 800, 800, false))
      verify(midiIO).sendNoteOn(0, 5, 112)
    }

    "should reflect updated calibration" in new SongData {
      preferences.handleCalibration.calibrationMinValue = 100
      preferences.handleCalibration.calibrationMaxValue = 110

      seismic.trigger(TriggerOnMessage("KICK", 800, 110, false))
      verify(midiIO).sendNoteOn(0, 5, 112)
    }
  }

  trait SongData {

    val midiIO = mock[MIDIIO]
    val preferences = Preferences(".")
    preferences.handleCalibration.calibrationMinValue = 0
    preferences.handleCalibration.calibrationMaxValue = 1023
    preferences.handleCalibration.inverted = false
    val triggeredState = new TriggeredState
    val seismic = new Seismic(midiIO, preferences, triggeredState)

    val kickInstrument1 = Instrument(Array("C#3"))
    val kickInstrument2 = Instrument(Array("F-2"))

    val snareInstrument1 = Instrument(Array("C#-2"))
    val snareInstrument2 = Instrument(Array("G8"))

    val phrase = Phrase("Test Phrase", 1)
    phrase.getInstrumentBankNamed("KICK").setInstruments(Array(kickInstrument1, kickInstrument2))
    phrase.getInstrumentBankNamed("SNARE").setInstruments(Array(snareInstrument1, snareInstrument2))

    val song = Song("Test Song", 1)
    song.setPhrases(Array(phrase))

    val setList = SetList("Test SetList")
    setList.songs = Array(song)

    seismic.setSetList(setList)

    seismic.setCurrentSong(song)
    seismic.setCurrentPhrase(phrase)
  }
}
