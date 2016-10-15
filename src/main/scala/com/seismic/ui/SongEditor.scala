package com.seismic.ui

import java.awt.event.{ActionEvent, KeyListener}
import java.awt.{Color, Dimension}
import javax.swing.JPanel

import com.daveclay.swing.util.Position._
import com.seismic.scala.ActionListenerExtensions._
import com.seismic.ui.utils.{HighlightOnFocus, LabeledTextField, SwingComponents}
import com.seismic.{Phrase, Song}

class SongEditor(onSongUpdated: (Song) => Unit,
                 onDeleteSong: (Song) => Unit,
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
  val deleteButton = SwingComponents.deleteButton()
  deleteButton.addActionListener((e: ActionEvent) => songOpt.foreach { song => onDeleteSong(song) })

  highlight(this).onFocusOf(nameField, channelField.inputField)

  position(label).at(4, 4).in(this)
  position(nameField).toTheRightOf(label).withMargin(10).in(this)
  position(channelField).toTheRightOf(nameField).withMargin(10).in(this)
  position(deleteButton).toTheRightOf(channelField).withMargin(10).in(this)

  def highlightBackgroundColor = backgroundColor

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
