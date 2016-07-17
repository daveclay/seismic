package com.seismic.ui.swing

import java.awt.Dimension
import javax.swing.{JLabel, JPanel}

import com.daveclay.swing.util.Position._

class ManualTriggerConfig(triggerName: String, size: Dimension) extends JPanel {
  SwingComponents.addBorder(this)
  setPreferredSize(size)

  val label = new JLabel(triggerName)
  val triggerValueField = new LabeledTextField("Trigger Value", 5, onChange)
  val handleValueField = new LabeledTextField("Handle Value", 5, onChange)

  position(label).at(4, 4).in(this)
  position(triggerValueField).toTheRightOf(label).withMargin(4).in(this)
  position(handleValueField).toTheRightOf(triggerValueField).withMargin(4).in(this)

  def onChange(value: String): Unit = {
    println(s"ok $value")
  }
}
