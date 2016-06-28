package com.seismic.ui.swing

import java.awt.{Color, Component, Graphics, Insets}
import java.awt.event._
import javax.swing.border.{Border, CompoundBorder, EmptyBorder, LineBorder}
import javax.swing._

object SwingComponents {

  class RoundedBorder(radius: Int) extends Border {
    def getBorderInsets(c: Component) = {
      new Insets(this.radius+1, this.radius+1, this.radius+2, this.radius)
    }

    def isBorderOpaque = {
      true
    }

    def paintBorder(c: Component, g: Graphics, x: Int, y: Int, width: Int, height: Int) {
      g.drawRoundRect(x, y, width-1, height-1, radius, radius);
    }
  }

  def addBorder(panel: JPanel) = {
    panel.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0)))
    panel
  }

  def addBorderWithMargin(panel: JPanel, margin: Int) = {
    val outerBorder = BorderFactory.createLineBorder(new Color(0, 0, 0))
    val marginBorder = BorderFactory.createEmptyBorder(margin, margin, margin, margin)
    panel.setBorder(BorderFactory.createCompoundBorder(outerBorder, marginBorder))
    panel
  }

  def configureButton(button: JButton): Unit = {
    button.setForeground(new Color(200, 200, 200))
    button.setBackground(Color.BLACK)
    button.setOpaque(true)
    button.addFocusListener(new FocusListener {
      override def focusGained(e: FocusEvent): Unit = {
        button.setBackground(new Color(250, 200, 0))
        button.setForeground(Color.BLACK)
      }
      override def focusLost(e: FocusEvent): Unit = {
        button.setBackground(Color.BLACK)
        button.setForeground(new Color(200, 200, 200))
      }
    })

    val line = new LineBorder(new Color(100, 100, 100))
    val margin = new EmptyBorder(5, 15, 5, 15)
    val compound = new CompoundBorder(line, margin)
    button.setBorder(compound)

  }

  def button(text: String) = {
    val button = new JButton(text)
    configureButton(button)
    button
  }

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
    // field.setEditable(false)
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
