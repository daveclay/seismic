package com.seismic.ui.swing

import java.awt.event.{MouseEvent, _}
import java.awt.{Color, Dimension}
import javax.swing.JPanel

import com.daveclay.swing.util.Position._

case class SelectionItem[T <: Selectable](item: T,
                                          itemWasClicked: () => Unit) extends JPanel {

  private val itemSize = new Dimension(248, 20)

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

  // TODO: I really need a way to indicate "navigation focused item" vs "currently playing item"

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
