package com.seismic.ui.swing

import java.awt.event.{KeyAdapter, KeyEvent}

class PhraseNavigationKeyListener(onUp: () => Unit,
                                  onDown: () => Unit,
                                  onNumber: (Int) => Unit) extends KeyAdapter {
  override def keyPressed(e: KeyEvent): Unit = Action(e).execute()

  case class Action(e: KeyEvent) {
    val consumeKeyUp = () => {
      onUp()
      e.consume()
    }

    val consumeKeyDown = () => {
      onDown()
      e.consume()
    }

    val consumeNumber = (i: Int) => {
      onNumber(i)
      e.consume()
    }

    def execute(): Unit = {
      e.getKeyCode match {
        case KeyEvent.VK_UP | KeyEvent.VK_KP_UP => consumeKeyUp()
        case KeyEvent.VK_DOWN | KeyEvent.VK_KP_DOWN => consumeKeyDown()
        case KeyEvent.VK_1 => consumeNumber(1)
        case KeyEvent.VK_2 => consumeNumber(2)
        case KeyEvent.VK_3 => consumeNumber(3)
        case KeyEvent.VK_4 => consumeNumber(4)
        case KeyEvent.VK_5 => consumeNumber(5)
        case KeyEvent.VK_6 => consumeNumber(6)
        case KeyEvent.VK_7 => consumeNumber(7)
        case KeyEvent.VK_8 => consumeNumber(8)
        case _ =>
      }
    }
  }
}
