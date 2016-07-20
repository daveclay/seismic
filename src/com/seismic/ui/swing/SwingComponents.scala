package com.seismic.ui.swing

import java.awt._
import java.awt.event._
import javax.swing.border.{Border, CompoundBorder, EmptyBorder, LineBorder}
import javax.swing._
import javax.swing.plaf.ButtonUI

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

  def buttonFocused(button: Component): Unit = {
    button.setBackground(new Color(250, 200, 0))
    button.setForeground(Color.BLACK)
  }

  def buttonBlurred(button: Component): Unit = {
    button.setBackground(Color.BLACK)
    button.setForeground(new Color(200, 200, 200))
  }

  def configureButton(button: JButton): Unit = {
    button.setForeground(new Color(200, 200, 200))
    button.setBackground(Color.BLACK)
    button.setOpaque(true)
    button.setFont(monoFont11)
    button.addFocusListener(new FocusListener {
      override def focusGained(e: FocusEvent): Unit = buttonFocused(button)
      override def focusLost(e: FocusEvent): Unit = buttonBlurred(button)
    })
    button.addKeyListener(new KeyAdapter() {
      override def keyPressed(e: KeyEvent): Unit = buttonActive(button)
      override def keyReleased(e: KeyEvent): Unit = buttonFocused(button)
    })
    button.addMouseListener(new MouseAdapter() {
      override def mousePressed(e: MouseEvent): Unit = buttonActive(button)
      override def mouseReleased(e: MouseEvent): Unit = buttonFocused(button)
    })

    val line = new LineBorder(new Color(100, 100, 100))
    val margin = new EmptyBorder(2, 8, 2, 8)
    val compound = new CompoundBorder(line, margin)
    button.setBorder(compound)
  }

  def buttonBlurred(button: JButton): Unit = {
    button.setBackground(Color.BLACK)
    button.setForeground(foregroundFontColor)
  }

  def buttonFocused(button: JButton): Unit = {
    button.setBackground(Color.BLACK)
    button.setForeground(buttonFocusFontColor)
  }

  def buttonActive(button: JButton): Unit = {
    button.setForeground(Color.BLACK)
    button.setBackground(buttonActiveColor)
  }

  def button(text: String) = {
    val button = new JButton(text)
    configureButton(button)
    button
  }

  def label(text: String): JLabel = {
    label(text, monoFont11)
  }

  def label(text: String, font: Font) = {
    val label = new JLabel(text)
    label.setFocusable(false)
    label.setFont(font)
    label.setForeground(foregroundFontColor)
    label
  }

  val titleSize = 23
  val titleFont = new Font("Arial", Font.PLAIN, titleSize)
  //val monoFont18 = new Font("PT Mono", Font.PLAIN, 18)
  // val monoFont11 = new Font("PT Mono", Font.PLAIN, 11)
  val fontName = "PT Mono"
  val monoFont11 = new Font(fontName, Font.PLAIN, 11)
  val monoFont18 = new Font(fontName, Font.PLAIN, 18)
  val backgroundColor = new Color(30, 30, 43)
  val componentBGColor = new Color(50, 50, 50)
  val foregroundFontColor = new Color(200, 200, 210)
  val buttonActiveColor = new Color(250, 200, 0)
  val buttonActiveFontColor = Color.BLACK
  val buttonFocusFontColor = buttonActiveColor

  def titleLabel(text: String) = {
    val title = label(text)
    title.setFont(titleFont)
    title.setForeground(foregroundFontColor)
    title
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
    field.setFont(monoFont11)

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
