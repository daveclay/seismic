package com.seismic.ui

import java.awt.event.{KeyAdapter, KeyEvent, MouseAdapter, MouseEvent}
import java.awt.{Color, Dimension}
import javax.swing.JPanel

import com.daveclay.swing.util.Position._
import com.seismic.ui.utils.{HighlightOnFocus, LabeledTextField, SwingComponents}

class ManualTriggerConfig(triggerName: String,
                          triggerOn: (String, Int, Int, Int) => Unit,
                          triggerOff: (String) => Unit,
                          size: Dimension) extends JPanel with HighlightOnFocus {
  SwingComponents.addBorder(this)
  setPreferredSize(size)
  setMinimumSize(size)

  val label = SwingComponents.label(triggerName, SwingComponents.monoFont18)
  label.setForeground(new Color(200, 200, 200))

  val triggerValueField = new LabeledTextField("Trigger Value", 5, onChange)
  triggerValueField.setText("0")

  val handleValueField = new LabeledTextField("Handle Value", 5, onChange)
  handleValueField.setText("0")

  val fingerField = new LabeledTextField("Finger", 2, onChange)
  fingerField.setText("0")

  val triggerButton = SwingComponents.button("FIRE")
  triggerButton.addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent): Unit = fire()
    override def mouseReleased(e: MouseEvent): Unit = off()
  })
  triggerButton.addKeyListener(new KeyAdapter() {
    override def keyPressed(e: KeyEvent): Unit = fire()
    override def keyReleased(e: KeyEvent): Unit = off()
  })

  highlight(this).onFocusOf(triggerValueField.inputField, handleValueField.inputField, triggerButton)

  position(label).at(4, 4).in(this)
  position(triggerValueField).toTheRightOf(label).withMargin(12).in(this)
  position(handleValueField).toTheRightOf(triggerValueField).withMargin(4).in(this)
  position(fingerField).toTheRightOf(handleValueField).withMargin(4).in(this)
  position(triggerButton).toTheRightOf(fingerField).withMargin(4).in(this)

  def onChange(value: String): Unit = {
  }

  def fire(): Unit = {
    triggerOn(triggerName, getTriggerValue, getHandleValue, getFingerValue)
  }

  def off(): Unit = {
    triggerOff(triggerName)
  }

  def highlightBackgroundColor = SwingComponents.componentBGColor

  private def getTriggerValue = triggerValueField.inputField.getText.toInt
  private def getHandleValue = handleValueField.inputField.getText.toInt
  private def getFingerValue = fingerField.inputField.getText.toInt
}
