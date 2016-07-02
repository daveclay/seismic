package com.seismic.ui.swing

import java.awt.event.{MouseEvent, _}
import java.awt.{Color, Dimension}
import javax.swing.JPanel

import com.daveclay.swing.util.Position._

case class Highlight(foreground: Color, background: Color)
object HighSelected extends Highlight(Color.BLACK, new Color(230, 190, 0))
object LowSelected extends Highlight(Color.BLACK, new Color(180, 180, 0))
object Deselected extends Highlight(new Color(200, 200, 200), Color.BLACK)

case class SelectionItem[T <: Selectable](item: T,
                                          itemWasClicked: () => Unit) extends JPanel {
  private val itemSize = new Dimension(248, 20)
  var highlight: Highlight = Deselected

  setPreferredSize(itemSize)
  setBackground(Color.BLACK)

  val nameLabel = SwingComponents.label(item.name)
  nameLabel.setPreferredSize(new Dimension(itemSize.width, 20))
  nameLabel.setForeground(new Color(200, 200, 200))

  position(nameLabel).at(4, 0).in(this)

  addMouseListener(new MouseListener {
    override def mouseExited(e: MouseEvent): Unit = {}
    override def mouseClicked(e: MouseEvent): Unit = {
      itemWasClicked()
    }
    override def mouseEntered(e: MouseEvent): Unit = {}
    override def mousePressed(e: MouseEvent): Unit = {
      setBackground(new Color(100, 40, 0))
      nameLabel.setForeground(Color.WHITE)
    }
    override def mouseReleased(e: MouseEvent): Unit = {}
  })

  def getHighlight = highlight

  def setHighlight(highlight: Highlight): Unit = {
    this.highlight = highlight
    setBackground(highlight.background)
    nameLabel.setForeground(highlight.foreground)
  }

  def setLabel(text: String): Unit = {
    nameLabel.setText(text)
  }
}
