package com.seismic.io

import java.io.{File, FileInputStream, FileWriter}

import com.fasterxml.jackson.core.`type`.TypeReference
import com.seismic.{Instrument, Phrase, SetList, Song}

object Migrator {
  def main(args: Array[String]): Unit = {
    val f = new File(args(0))

    val result = ObjectMapperFactory.objectMapper.readValue(f, classOf[FromSetList])

    val setList = SetList(result.name)
    setList.songs = result.songs.map { fromSong =>
      val song = Song(fromSong.name, fromSong.channel)
      song.setPhrases(fromSong.phrases.map { fromPhrase =>
        val phrase = Phrase(fromPhrase.name, fromPhrase.patch)
        phrase.getInstrumentBankNamed("KICK").setInstruments(fromPhrase.instrumentBanks.KICK.instruments.map { fromInstrument => Instrument(fromInstrument.notes) })
        phrase.getInstrumentBankNamed("ALTKICK").setInstruments(fromPhrase.instrumentBanks.ALTKICK.instruments.map { fromInstrument => Instrument(fromInstrument.notes) })
        phrase.getInstrumentBankNamed("SNARE").setInstruments(fromPhrase.instrumentBanks.SNARE.instruments.map { fromInstrument => Instrument(fromInstrument.notes) })
        phrase.getInstrumentBankNamed("ALTSNARE").setInstruments(fromPhrase.instrumentBanks.ALTSNARE.instruments.map { fromInstrument => Instrument(fromInstrument.notes) })
        phrase
      })
      song
    }

    val backup = new File(args(0) + ".old")
    f.renameTo(backup)

    ObjectMapperFactory.objectMapper.writeValue(new File(args(0)), setList)
  }

  case class FromSetList(name: String, songs: Array[FromSong])
  case class FromSong(name: String, channel: Int, phrases: Array[FromPhrase])
  case class FromPhrase(name: String, patch: Int,
                        instrumentBanks: FromInstrumentBanks)
  case class FromInstrumentBanks(KICK: FromInstrumentBank, SNARE: FromInstrumentBank,
                        ALTKICK: FromInstrumentBank, ALTSNARE: FromInstrumentBank)
  case class FromInstrumentBank(name: String, instruments: Array[FromInstrument])
  case class FromInstrument(notes: Array[String])
}
