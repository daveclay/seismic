package com.seismic.ui.swing

import java.awt.event.{KeyAdapter, KeyEvent}

class PhraseNavigationKeyListener(onUp: () => Unit,
                                  onDown: () => Unit,
                                  onNumber: (Int) => Unit,
                                  fireTrigger: (Int) => Unit,
                                  releaseTrigger: (Int) => Unit
                                 ) extends KeyAdapter {
  override def keyPressed(e: KeyEvent): Unit = Action(e).keyPressed()
  override def keyReleased(e: KeyEvent): Unit = Action(e).keyReleased()

  case class Action(e: KeyEvent) {
    val consume = (f: () => Unit) => {
      f()
      e.consume()
    }

    val consumeNumber = (i: Int) => consume(() => onNumber(i))

    def keyPressed(): Unit = {
      e.getKeyCode match {
        case KeyEvent.VK_UP | KeyEvent.VK_KP_UP => consume(() => onUp())
        case KeyEvent.VK_DOWN | KeyEvent.VK_KP_DOWN => consume(() => onDown())
        case KeyEvent.VK_1 => consumeNumber(1)
        case KeyEvent.VK_2 => consumeNumber(2)
        case KeyEvent.VK_3 => consumeNumber(3)
        case KeyEvent.VK_4 => consumeNumber(4)
        case KeyEvent.VK_5 => consumeNumber(5)
        case KeyEvent.VK_6 => consumeNumber(6)
        case KeyEvent.VK_7 => consumeNumber(7)
        case KeyEvent.VK_8 => consumeNumber(8)
        case KeyEvent.VK_S => consume(() => fireTrigger(e.getKeyCode))
        case KeyEvent.VK_K => consume(() => fireTrigger(e.getKeyCode))
        case _ =>
      }
    }

    def keyReleased(): Unit = {
      e.getKeyCode match {
        case KeyEvent.VK_S => consume(() => releaseTrigger(e.getKeyCode))
        case KeyEvent.VK_K => consume(() => releaseTrigger(e.getKeyCode))
        case _ =>
      }
    }
  }
}
