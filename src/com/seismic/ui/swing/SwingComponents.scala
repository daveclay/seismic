package com.seismic.ui.swing

import java.awt.Color
import java.awt.event._
import javax.swing.{BorderFactory, JLabel, JTextField}

object SwingComponents {

  def label(text: String) = {
    val label = new JLabel(text)
    label.setFocusable(false)
    label
  }

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

    val triggerValueChange = () => {
      field.setEditable(false)
      field.getCaret.setVisible(false)
      onValueChange(field.getText)
    }

    field.addFocusListener(new FocusListener {
      override def focusGained(e: FocusEvent): Unit = {
        field.setEditable(true)
        field.getCaret.setVisible(true)
        field.getCaret.setSelectionVisible(true)
      }

      override def focusLost(e: FocusEvent): Unit = {
        triggerValueChange()
      }
    })

    field.addActionListener((e: ActionEvent) => {
      triggerValueChange()
    })

    field
  }
}
