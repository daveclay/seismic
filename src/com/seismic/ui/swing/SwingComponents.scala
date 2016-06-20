package com.seismic.ui.swing

import java.awt.Color
import java.awt.event.{ActionEvent, ActionListener, MouseEvent, MouseListener}
import javax.swing.{BorderFactory, JTextField}

object SwingComponents {

  def textField(backgroundColor: Color, size: Int): JTextField = {
    textField(backgroundColor, size, (value) => {})
  }

  def textField(backgroundColor: Color,
                size: Int,
                onValueChange: String => Unit) = {
    val field = new JTextField(size)
    field.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2))
    field.setForeground(new Color(200, 200, 200))
    field.setBackground(backgroundColor)
    field.setOpaque(true)
    field.setEditable(false)
    field.setCaretColor(new Color(250, 250, 20))

    field.addMouseListener(new MouseListener() {
      override def mouseExited(e: MouseEvent): Unit = {}

      override def mouseClicked(e: MouseEvent): Unit = {
        field.selectAll()
        field.setEditable(true)
        field.getCaret.setVisible(true)
        field.getCaret.setSelectionVisible(true)
      }

      override def mouseEntered(e: MouseEvent): Unit = {}
      override def mousePressed(e: MouseEvent): Unit = {}
      override def mouseReleased(e: MouseEvent): Unit = {}
    })

    field.addActionListener((e: ActionEvent) => {
      field.setEditable(false)
      field.getCaret.setVisible(false)
      onValueChange(field.getText)
    })

    field
  }
}
