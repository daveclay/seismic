package com.seismic.ui.swing

import java.awt.event.KeyListener
import java.awt.{Color, Dimension}
import javax.swing.JPanel

import com.daveclay.swing.util.Position._
import com.seismic.{Phrase, Song}

class SongEditor(onSongUpdated: (Song) => Unit,
                 size: Dimension,
                 val backgroundColor: Color) extends JPanel with HighlightOnFocus {

  setPreferredSize(size)
  setBackground(backgroundColor)
  SwingComponents.addBorder(this)

  var songOpt: Option[Song] = None
  var currentPhraseOpt: Option[Phrase] = None

  val onNameChange = (name: String) => songOpt.foreach { song =>
    song.setName(name)
    onSongUpdated(song)
  }

  val onChannelChange = (channel: String) => songOpt.foreach { song =>
    song.setChannel(channel.toInt)
    onSongUpdated(song)
  }

  val label = SwingComponents.label("SONG", SwingComponents.monoFont18)
  val nameField = SwingComponents.textField(Color.BLACK, 30, onNameChange)
  val channelField = new LabeledTextField("MIDI Channel", 3, onChannelChange)

  highlight(this).onFocusOf(nameField, channelField.inputField)

  position(label).at(4, 4).in(this)
  position(nameField).toTheRightOf(label).withMargin(10).in(this)
  position(channelField).toTheRightOf(nameField).withMargin(10).in(this)

  def setSong(song: Song): Unit = {
    this.songOpt = Option(song)

    nameField.setText(song.name)
    channelField.setText(song.channel.toString)
  }

  override def addKeyListener(keyListener: KeyListener): Unit = {
    super.addKeyListener(keyListener)
    nameField.addKeyListener(keyListener)
    channelField.addKeyListener(keyListener)
  }
}
