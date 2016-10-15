package com.seismic.ui.utils.draglist

import java.awt.event.{KeyAdapter, KeyEvent}
import javax.swing.{DefaultListModel, JList}

class SelectionListKeyAdapter[T](jlist: JList[T],
                                 listModel: DefaultListModel[T],
                                 fireAccept: () => Unit,
                                 onBackout: () => Unit,
                                 onSelectBeforeFirst: () => Unit,
                                 onSelectAfterLast: () => Unit
                                ) extends KeyAdapter {

  override def keyPressed(e: KeyEvent){
    val code = e.getKeyCode
    if (code == KeyEvent.VK_ENTER
      || code == KeyEvent.VK_SPACE
      || code == KeyEvent.VK_RIGHT
      || code == KeyEvent.VK_KP_RIGHT) {
      fireAccept()
    } else if (code == KeyEvent.VK_LEFT
      || code == KeyEvent.VK_KP_LEFT) {
      onBackout()
    } else if (code == KeyEvent.VK_UP
      || code == KeyEvent.VK_KP_UP) {
      if (jlist.getSelectedIndex == 0) {
        e.consume()
        if (onSelectBeforeFirst != null) {
          onSelectBeforeFirst()
        } else {
          jlist.setSelectedIndex(listModel.getSize - 1)
        }
      }
    } else if (code == KeyEvent.VK_DOWN
      || code == KeyEvent.VK_KP_DOWN) {
      if (jlist.getSelectedIndex == listModel.getSize - 1) {
        e.consume()
        if (onSelectAfterLast != null) {
          onSelectAfterLast()
        } else {
          jlist.setSelectedIndex(0)
        }
      }
    }
  }
}
