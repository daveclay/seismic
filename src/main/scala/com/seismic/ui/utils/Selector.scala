package com.seismic.ui.utils

import java.awt.Dimension
import javax.swing._
import java.awt.Color

import com.seismic.Song
import com.seismic.ui.utils.draglist.{CellState, OrderableSelectionList}
import com.daveclay.swing.util.Position._
import com.seismic.ui.utils.draglist.OrderableSelectionList
import com.seismic.{Phrase, Song}

class Selector(songSelect: OrderableSelectionList[Song],
               phraseSelect: OrderableSelectionList[Phrase]) extends JPanel {

  setPreferredSize(new Dimension(Sizing.fitWidth(songSelect, phraseSelect) + 4, 600))

  position(songSelect).atOrigin().withMargin(4).in(this)
  position(phraseSelect).toTheRightOf(songSelect).withMargin(4).in(this)
}

object Selector {
  trait Selectable {
    def label: String
  }

  case class PhraseSelectable(phrase: Phrase) extends Selectable {
    def label = f"${phrase.patch} ${phrase.name}"
  }

  case class SongSelectable(song: Song) extends Selectable {
    def label = song.name
  }

  def renderAddButton(): JButton = {
    val button = SwingComponents.button("Add")
    button.setBorder(BorderFactory.createLineBorder(SwingComponents.componentBGColor))
    button.setFont(SwingComponents.monoFont18)
    button
  }

  def renderSongItem(song: Song, cellState: CellState) = {
    renderSelectable(SongSelectable(song), cellState)
  }

  def renderPhraseItem(phrase: Phrase, cellState: CellState) = {
    renderSelectable(PhraseSelectable(phrase), cellState)
  }

  def renderSelectable(selectable: Selectable, cellState: CellState) = {
    val panel = new ItemPanel
    val label = SwingComponents.label(selectable.label, SwingComponents.monoFont18)
    if (cellState.isSelected) {
      panel.setBackground(SwingComponents.highlightColor)
      label.setText(f">>> ${label.getText}")
      label.setFont(SwingComponents.monoFont18Bold)
      label.setForeground(panel.getForeground)
    } else {
      panel.setBackground(Color.BLACK)
      label.setForeground(SwingComponents.foregroundFontColor)
    }
    position(label).at(4, 4).in(panel)
    panel
  }

  class ItemPanel extends JPanel {
    setBorder(BorderFactory.createLineBorder(SwingComponents.componentBGColor))
  }
}

