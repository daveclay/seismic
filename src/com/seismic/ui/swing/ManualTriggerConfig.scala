package com.seismic.ui.swing

import java.awt.event.{KeyAdapter, KeyEvent, MouseAdapter, MouseEvent}
import java.awt.{Color, Dimension}
import javax.swing.{JLabel, JPanel}

import com.daveclay.swing.util.Position._

class ManualTriggerConfig(triggerName: String,
                          triggerOn: (String, Int, Int) => Unit,
                          triggerOff: (String) => Unit,
                          size: Dimension) extends JPanel {
  SwingComponents.addBorder(this)
  setPreferredSize(size)

  val label = SwingComponents.label(triggerName, SwingComponents.monoFont18)
  label.setForeground(new Color(200, 200, 200))

  val triggerValueField = new LabeledTextField("Trigger Value", 5, onChange)
  triggerValueField.setText("0")

  val handleValueField = new LabeledTextField("Handle Value", 5, onChange)
  handleValueField.setText("0")

  val triggerButton = SwingComponents.button("FIRE")
  triggerButton.addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent): Unit = triggerOn(triggerName, getTriggerValue, getHandleValue)
    override def mouseReleased(e: MouseEvent): Unit = triggerOff(triggerName)
  })
  triggerButton.addKeyListener(new KeyAdapter() {
    override def keyPressed(e: KeyEvent): Unit = triggerOn(triggerName, getTriggerValue, getHandleValue)
    override def keyReleased(e: KeyEvent): Unit = triggerOff(triggerName)
  })

  position(label).at(4, 4).in(this)
  position(triggerValueField).toTheRightOf(label).withMargin(12).in(this)
  position(handleValueField).toTheRightOf(triggerValueField).withMargin(4).in(this)
  position(triggerButton).toTheRightOf(handleValueField).withMargin(4).in(this)

  def onChange(value: String): Unit = {
  }

  private def getTriggerValue = triggerValueField.inputField.getText.toInt
  private def getHandleValue = handleValueField.inputField.getText.toInt
}
