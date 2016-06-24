package com.seismic.ui.swing

import java.awt.event.{MouseEvent, _}
import java.awt.{Color, Dimension}
import javax.swing.JPanel

import com.daveclay.swing.util.Position._
import com.seismic.Phrase

case class PhraseItem(phrase: Phrase,
                      onShowSelected: () => Unit,
                      onEditPhraseSelected: () => Unit,
                      onSelectPrevious: () => Unit,
                      onSelectNext: () => Unit) extends JPanel {

  private val itemSize = new Dimension(250, 20)

  setFocusable(true)
  setPreferredSize(itemSize)
  setBackground(Color.BLACK)

  val nameLabel = SwingComponents.label(phrase.name)
  nameLabel.setPreferredSize(new Dimension(itemSize.width, 20))
  nameLabel.setForeground(new Color(200, 200, 200))

  position(nameLabel).at(4, 0).in(this)

  addFocusListener(new FocusListener {
    override def focusGained(e: FocusEvent): Unit = {
      onShowSelected()
    }

    override def focusLost(e: FocusEvent): Unit = {
    }
  })

  addKeyListener(new KeyListener {
    override def keyTyped(e: KeyEvent): Unit = {}
    override def keyReleased(e: KeyEvent): Unit = {}
    override def keyPressed(e: KeyEvent): Unit = {
      val code = e.getKeyCode

      if (code == KeyEvent.VK_UP || code == KeyEvent.VK_KP_UP) {
        onSelectPrevious()
      } else if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_KP_DOWN) {
        onSelectNext()
      } else if (code == KeyEvent.VK_RIGHT || code == KeyEvent.VK_KP_RIGHT) {
        onEditPhraseSelected()
      } else if (code == KeyEvent.VK_SPACE) {
        onEditPhraseSelected()
      }
    }
  })

  addMouseListener(new MouseListener {
    override def mouseExited(e: MouseEvent): Unit = {}
    override def mouseClicked(e: MouseEvent): Unit = {
      onEditPhraseSelected()
    }
    override def mouseEntered(e: MouseEvent): Unit = {}
    override def mousePressed(e: MouseEvent): Unit = {
      setBackground(new Color(100, 40, 0))
      nameLabel.setForeground(Color.WHITE)
    }
    override def mouseReleased(e: MouseEvent): Unit = {}
  })

  def indicateSelected() = {
    setBackground(new Color(230, 190, 0))
    nameLabel.setForeground(Color.BLACK)
  }

  def indicateUnselect(): Unit = {
    setBackground(Color.BLACK)
    nameLabel.setForeground(new Color(200, 200, 200))
  }

  def setLabel(text: String): Unit = {
    nameLabel.setText(text)
  }
}
