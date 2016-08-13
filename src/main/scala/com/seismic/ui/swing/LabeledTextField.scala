package com.seismic.ui.swing

import java.awt.event.KeyListener
import java.awt.{Color, Dimension}
import javax.swing.{JLabel, JPanel}

import com.daveclay.swing.util.Position._

object LabeledTextField {
  trait Orientation
  object Vertical extends Orientation
  object Horizontal extends Orientation
}

class LabeledTextField(labelText: String,
                       size: Int,
                       orientation: LabeledTextField.Orientation,
                       margin: Int,
                       onValueChange: String => Unit) extends JPanel {

  def this(labelText: String, size: Int, onValueChange: String => Unit) {
    this(labelText, size, LabeledTextField.Horizontal, margin = 10, onValueChange)
  }

  setOpaque(false)

  val label = SwingComponents.label(labelText)
  label.setForeground(new Color(200, 200, 200))

  val inputField = SwingComponents.textField(Color.BLACK, size, onValueChange)

  val labelSize = label.getPreferredSize
  var textFieldSize = inputField.getPreferredSize

  position(label).atOrigin().in(this)

  if (orientation == LabeledTextField.Horizontal) {
    setPreferredSize(new Dimension(labelSize.width + textFieldSize.width + 10, textFieldSize.height))
    position(inputField).toTheRightOf(label).withMargin(margin).in(this)
  } else {
    setPreferredSize(new Dimension(Math.max(labelSize.width, textFieldSize.width), textFieldSize.height + labelSize.height))
    position(inputField).below(label).withMargin(margin).in(this)
  }

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
