package com.seismic.ui.swing

import java.awt.Color
import java.awt.event.ActionEvent
import javax.swing.{AbstractAction, JButton, JMenuItem, JPopupMenu}

import scala.collection.mutable.ArrayBuffer

class SMenuButton[T](text: String, onSelected: (T) => Unit) extends JButton(text) {

  case class MenuItemIndex(item: JMenuItem, index: Int)

  val popup = new JPopupMenu()
  popup.setOpaque(true)
  popup.setBackground(Color.BLACK)
  popup.setForeground(new Color(200, 200, 200))
  addActionListener( (e) =>
    popup.show(this, 7, getHeight - 4)
  )

  val items = ArrayBuffer[MenuItemIndex]()

  SwingComponents.configureButton(this)

  def addItem(text: String, value: T): Unit = {

    val item = new JMenuItem(new AbstractAction(text) {
      def actionPerformed(e: ActionEvent): Unit = {
        onSelected(value)
      }
    })

    items :+ MenuItemIndex(item, items.size)
    popup.add(item)
    popup.revalidate()
  }

  def removeItems() = {
    popup.removeAll()
    items.clear()
    popup.revalidate()
  }
}
