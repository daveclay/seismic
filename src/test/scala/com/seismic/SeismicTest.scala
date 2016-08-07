package com.seismic

import com.seismic.io.Preferences
import com.seismic.messages.TriggerOnMessage
import com.seismic.midi.MIDIIO
import com.seismic.test.Test
import org.mockito.Mockito._

class SeismicTest extends Test {

  val midiIO = mock[MIDIIO]
  val preferences = Preferences(".")
  val seismic = new Seismic(midiIO, preferences)
  val kickInstrument1 = Instrument(Array("C#3"))
  val kickInstrument2 = Instrument(Array("F-2"))
  val snareInstrument1 = Instrument(Array("C#-2"))
  val snareInstrument2 = Instrument(Array("G8"))

  val phrase = Phrase("Test Phrase", 1)
  kickInstrument1.phrase = phrase
  kickInstrument2.phrase = phrase
  snareInstrument1.phrase = phrase
  snareInstrument2.phrase = phrase

  phrase.setKickInstruments(Array(kickInstrument1, kickInstrument2))
  phrase.setSnareInstruments(Array(snareInstrument1, snareInstrument2))

  val song = Song("Test Song", 1)
  phrase.song = song
  song.phrases = Array(phrase)

  seismic.setCurrentSong(song)
  seismic.setCurrentPhrase(phrase)

  "Seismic" should "trigger a midi note for the first kick instrument" in {
    seismic.trigger(TriggerOnMessage("KICK", 1023, 0))
    verify(midiIO).sendNoteOn(0, 61, 127)
  }

  it should "trigger a midi note for the second kick instrument" in {
    seismic.trigger(TriggerOnMessage("KICK", 1023, 1023))
    verify(midiIO).sendNoteOn(0, 5, 127)
  }

  it should "trigger a midi note for the first snare instrument" in {
    seismic.trigger(TriggerOnMessage("SNARE", 1023, 0))
    verify(midiIO).sendNoteOn(0, 1, 127)
  }

  it should "trigger a midi note for the second snare instrument" in {
    seismic.trigger(TriggerOnMessage("SNARE", 1023, 1023))
    verify(midiIO).sendNoteOn(0, 127, 127)
  }
}
