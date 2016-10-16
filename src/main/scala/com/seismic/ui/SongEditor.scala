package com.seismic.ui

import java.awt.event.{ActionEvent, KeyListener}
import java.awt.{Color, Dimension, GridBagConstraints, Insets}
import javax.swing.{BorderFactory, JPanel}

import com.daveclay.swing.util.Position._
import com.seismic.scala.ActionListenerExtensions._
import com.seismic.ui.utils.layout.GridBagLayoutHelper
import com.seismic.ui.utils.{HighlightOnFocus, LabeledTextField, SwingComponents}
import com.seismic.{Phrase, Song}

class SongEditor(onSongUpdated: (Song) => Unit,
                 onDeleteSong: (Song) => Unit,
                 size: Dimension,
                 val backgroundColor: Color) extends JPanel with HighlightOnFocus {

  setPreferredSize(size)
  setMinimumSize(size)

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
  label.setMinimumSize(label.getPreferredSize)
  val nameField = SwingComponents.textField(Color.BLACK, 30, onNameChange)
  val channelField = new LabeledTextField("MIDI Channel", 3, onChannelChange)
  val deleteButton = SwingComponents.deleteButton()
  deleteButton.addActionListener((e: ActionEvent) => songOpt.foreach { song => onDeleteSong(song) })

  highlight(this).onFocusOf(nameField, channelField.inputField)

  val helper = new GridBagLayoutHelper(this)

  val pad = new Insets(4, 4, 0, 0)
  helper.position(label).withPadding(4).alignLeft().atOrigin().inParent()
  helper.position(nameField).withPadding(pad).nextTo(label).alignLeft().fillHorizontal().weightX(1).inParent()
  helper.position(channelField).withPadding(pad).nextTo(nameField).alignLeft().inParent()
  helper.position(deleteButton).withPadding(pad).nextTo(channelField).alignLeft().inParent()
  helper.horizontalSpacer().weightX(.1f).nextTo(deleteButton).inParent()

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
