package com.seismic.ui.swing

import java.awt.event.{MouseEvent, _}
import java.awt.{Color, Dimension}
import javax.swing.JPanel

import com.daveclay.swing.util.Position._
import com.seismic.Phrase

case class AddPhraseItem(onAddPhrase:() => Unit,
                         onSelectPrevious: () => Unit,
                         onSelectNext: () => Unit)
  extends PhraseItem("Add Phrase",
                      () => onAddPhrase(),
                      () => Unit,
                      () => Unit,
                      onSelectPrevious,
                      onSelectNext)

case class SelectPhraseItem(phrase: Phrase,
                            onSelected: () => Unit,
                            onEditPhraseClicked: () => Unit,
                            onSelectPrevious: () => Unit,
                            onSelectNext: () => Unit)
  extends PhraseItem(phrase.name,
                      onSelected,
                      onSelected,
                      onEditPhraseClicked,
                      onSelectPrevious,
                      onSelectNext)

class PhraseItem(name: String,
                 onClick: () => Unit,
                 onFocus: () => Unit,
                 onEditPhraseClicked: () => Unit,
                 onSelectPrevious: () => Unit,
                 onSelectNext: () => Unit) extends JPanel() {
  setFocusable(true)
  setPreferredSize(new Dimension(140, 20))
  setBackground(Color.BLACK)

  val nameLabel = SwingComponents.label(name)
  nameLabel.setForeground(new Color(200, 200, 200))

  position(nameLabel).atOrigin().in(this)

  addFocusListener(new FocusListener {
    override def focusGained(e: FocusEvent): Unit = {
      setBackground(new Color(230, 190, 0))
      nameLabel.setForeground(Color.BLACK)
      onFocus()
    }

    override def focusLost(e: FocusEvent): Unit = {
      setBackground(Color.BLACK)
      nameLabel.setForeground(new Color(200, 200, 200))
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
        onEditPhraseClicked()
      } else if (code == KeyEvent.VK_SPACE) {
        onClick()
      }
    }
  })

  addMouseListener(new MouseListener {
    override def mouseExited(e: MouseEvent): Unit = {}
    override def mouseClicked(e: MouseEvent): Unit = {
      onClick()
    }
    override def mouseEntered(e: MouseEvent): Unit = {}
    override def mousePressed(e: MouseEvent): Unit = {
      setBackground(new Color(250, 240, 100))
      nameLabel.setForeground(Color.BLACK)
    }
    override def mouseReleased(e: MouseEvent): Unit = {
      unselect()
    }
  })

  def unselect(): Unit = {
    setBackground(Color.BLACK)
    nameLabel.setForeground(new Color(200, 200, 200))
  }

  def select(): Unit = {
    this.grabFocus()
  }

  def setLabel(text: String): Unit = {
    nameLabel.setText(text)
  }
}
