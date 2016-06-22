package com.seismic.ui.swing

import java.awt.Color
import java.awt.event.ActionEvent
import javax.swing.{AbstractAction, JButton, JMenuItem, JPopupMenu}

import scala.collection.mutable.ArrayBuffer

class SMenuButton[T](text: String, onSelected: (T) => Unit) extends JButton(text) {

  val popup = new JPopupMenu()
  popup.setOpaque(true)
  popup.setBackground(Color.BLACK)
  popup.setForeground(new Color(200, 200, 200))

  val items = ArrayBuffer[JMenuItem]()

  SwingComponents.configureButton(this)

  def addItem(text: String, value: T): Unit = {

    val item = new JMenuItem(new AbstractAction(text) {
      def actionPerformed(e: ActionEvent): Unit = {
        onSelected(value)
      }
    })

    items :+ items
    popup.add(item)
  }

  addActionListener( (e) =>
    popup.show(this, 7, getHeight - 4)
  )
}
