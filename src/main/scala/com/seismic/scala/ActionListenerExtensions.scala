package com.seismic.scala

import java.awt.event.{ActionEvent, ActionListener}
import javax.swing._

object ActionListenerExtensions {

  implicit def extendJButton(button: JButton): AbstractButtonExtensions = new AbstractButtonExtensions(button)
  implicit def extendJMenuItem(button: JMenuItem): AbstractButtonExtensions = new AbstractButtonExtensions(button)
  implicit def extendJTextField(textField: JTextField): JTextFieldExtensions = new JTextFieldExtensions(textField)

  class JTextFieldExtensions(val textField: JTextField) {
    def addActionListener(f: (ActionEvent) => Unit) {
      textField.addActionListener(new ActionListener {
        override def actionPerformed(e: ActionEvent): Unit = f(e)
      })
    }
  }

  class AbstractButtonExtensions(val button: AbstractButton) {
    def addActionListener(f: (ActionEvent) => Unit) {
      button.addActionListener(new ActionListener {
        override def actionPerformed(e: ActionEvent): Unit = f(e)
      })
    }
  }
}
