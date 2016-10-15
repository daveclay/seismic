package com.seismic.ui.utils

import java.awt.Toolkit
import java.awt.event.{ActionEvent, ActionListener}
import javax.swing.{JMenu, JMenuItem, KeyStroke}
import com.seismic.scala.ActionListenerExtensions._

class SMenu(name: String) extends JMenu(name) {

  def addItem(label: String, acceleratorMnemonicKey: Int, action: () => Unit) = {
    val menuItem = new JMenuItem(label)
    menuItem.setAccelerator(KeyStroke.getKeyStroke(acceleratorMnemonicKey,
                                                    Toolkit.getDefaultToolkit.getMenuShortcutKeyMask))
    menuItem.setMnemonic(acceleratorMnemonicKey)
    menuItem.addActionListener((e: ActionEvent) => action() )
    this.add(menuItem)
  }
}
