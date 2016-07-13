package com.seismic.ui.swing

import java.awt.event.{KeyAdapter, KeyEvent}

class PhraseNavigationKeyListener(onUp: () => Unit,
                                  onDown: () => Unit,
                                  onNumber: (Int) => Unit) extends KeyAdapter {
  override def keyPressed(e: KeyEvent): Unit = {

    // TODO: patch/onNumber

    val code = e.getKeyCode
    if (code == KeyEvent.VK_UP || code == KeyEvent.VK_KP_UP) {
      onUp()
      e.consume()
    } else if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_KP_DOWN) {
      onDown()
      e.consume()
    }
  }
}
