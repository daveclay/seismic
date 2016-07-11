package com.seismic.ui.swing

import java.awt.event.KeyListener
import java.awt.{Color, Dimension}
import javax.swing.{JLabel, JPanel}

import com.daveclay.swing.util.Position._

class LabeledTextField(labelText: String,
                       backgroundColor: Color,
                       size: Int,
                       onValueChange: String => Unit) extends JPanel {

  setOpaque(false)

  val label = SwingComponents.label(labelText)
  label.setForeground(new Color(200, 200, 200))

  val inputField = SwingComponents.textField(Color.BLACK, size, onValueChange)

  val labelSize = label.getPreferredSize
  var textFieldSize = inputField.getPreferredSize

  setPreferredSize(new Dimension(labelSize.width + textFieldSize.width, textFieldSize.height))

  position(label).atOrigin().in(this)
  position(inputField).toTheRightOf(label).withMargin(10).in(this)

  def highlightField(): Unit = {
    inputField.setBackground(new Color(170, 170, 170))
    inputField.setForeground(Color.BLACK)
  }

  def unhighlightField(): Unit = {
    inputField.setBackground(Color.BLACK)
    inputField.setForeground(new Color(200, 200, 200))
  }

  def setText(text: String): Unit = {
    inputField.setText(text)
  }

  override def grabFocus(): Unit = {
    inputField.grabFocus()
  }

  override def addKeyListener(keyListener: KeyListener): Unit = {
    inputField.addKeyListener(keyListener)
  }

  def getTextField = inputField
}
