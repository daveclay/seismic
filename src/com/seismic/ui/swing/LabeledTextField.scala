package com.seismic.ui.swing

import java.awt.{Color, Dimension}
import javax.swing.{JLabel, JPanel}

import com.daveclay.swing.util.Position._

class LabeledTextField(labelText: String,
                       backgroundColor: Color,
                       size: Int,
                       onValueChange: String => Unit) extends JPanel {

  val label = SwingComponents.label(labelText)
  label.setBackground(backgroundColor)
  label.setForeground(new Color(200, 200, 200))

  setBackground(backgroundColor)

  val textField = SwingComponents.textField(Color.BLACK, size, onValueChange)

  val labelSize = label.getPreferredSize
  var textFieldSize = textField.getPreferredSize

  setPreferredSize(new Dimension(labelSize.width + textFieldSize.width, textFieldSize.height))

  position(label).atOrigin().in(this)
  position(textField).toTheRightOf(label).withMargin(10).in(this)

  def setText(text: String): Unit = {
    textField.setText(text)
  }
}
