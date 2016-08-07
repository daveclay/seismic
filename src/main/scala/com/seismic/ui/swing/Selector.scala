package com.seismic.ui.swing

import java.awt.Dimension
import javax.swing.JPanel
import java.awt.Color
import javax.swing.{BorderFactory, JLabel, JPanel}

import com.seismic.Song
import com.seismic.ui.swing.draglist.{CellState, OrderableSelectionList}
import com.daveclay.swing.util.Position._
import com.seismic.ui.swing.draglist.OrderableSelectionList
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
    def label = phrase.name
  }

  case class SongSelectable(song: Song) extends Selectable {
    def label = song.name
  }

  def renderSongItem(song: Song, cellState: CellState) = {
    renderSelectable(SongSelectable(song), cellState)
  }

  def renderPhraseItem(phrase: Phrase, cellState: CellState) = {
    renderSelectable(PhraseSelectable(phrase), cellState)
  }

  def renderSelectable(selectable: Selectable, cellState: CellState) = {
    val panel = new JPanel
    panel.setBorder(BorderFactory.createLineBorder(Color.GRAY))
    val label = SwingComponents.label(selectable.label, SwingComponents.monoFont18)
    if (cellState.isSelected) {
      panel.setBackground(SwingComponents.highlightColor)
      label.setForeground(panel.getForeground)
    } else {
      panel.setBackground(Color.BLACK)
      label.setForeground(SwingComponents.foregroundFontColor)
    }
    panel.add(label)
    panel
  }
}

