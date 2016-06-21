package com.seismic.ui.swing

import java.awt.Toolkit
import java.awt.event.ActionEvent
import javax.swing.{JMenu, JMenuItem, KeyStroke}

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
