package com.seismic.ui.utils

import java.awt.event.KeyListener
import java.awt.{Color, Dimension, Insets}
import javax.swing.{JLabel, JPanel}

import com.daveclay.swing.util.Position._
import com.seismic.ui.utils.layout.GridBagLayoutHelper

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

  val helper = new GridBagLayoutHelper(this)
  helper.position(label).atOrigin().alignLeft().inParent()

  if (orientation == LabeledTextField.Horizontal) {
    val dimension = new Dimension(labelSize.width + textFieldSize.width + 10, textFieldSize.height)
    setPreferredSize(dimension)
    setMinimumSize(dimension)
    helper.position(inputField).nextTo(label).withPadding(new Insets(0, margin, 0, 0)).fillHorizontal().weightX(1).inParent()

  } else {
    val dimension = new Dimension(Math.max(labelSize.width, textFieldSize.width),
                                              textFieldSize.height + labelSize.height)
    setPreferredSize(dimension)
    setMinimumSize(dimension)
    helper.position(inputField).nextTo(label).withPadding(new Insets(0, margin, 0, 0)).fillHorizontal().weightX(1).inParent()
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
