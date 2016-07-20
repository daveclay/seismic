package com.seismic.ui.swing

import java.awt._
import java.awt.event._

import com.daveclay.swing.color.ColorUtils

trait HighlightOnFocus extends Component {

  def highlightBackgroundColor: Color
  val highlightThisComponentOnFocusListener = highlightComponentFocusListener(this)

  def highlight(components: Component*) = {
    removeHighlightFocusListeners(components)
    HighlightOnFocusBuilder(components)
  }

  case class HighlightOnFocusBuilder(components: Seq[Component]) {
    val listeners = components.map { component => highlightComponentFocusListener(component) }

    def onFocusOf(receivers: Component*) = {
      andFocusOf(receivers)
      this
    }

    def andFocusOf(receivers: Seq[Component]) = {
      receivers.foreach { receiver =>
        listeners.foreach { listener =>
          receiver.addFocusListener(listener)
        }
      }
      this
    }
  }

  addFocusListener(highlightThisComponentOnFocusListener)

  private def getHighlightFocusListeners(component: Component)  = {
    component.getFocusListeners.filter { listener => listener.isInstanceOf[HighlightingFocusListener]}
  }

  private def removeHighlightFocusListeners(components: Seq[Component]): Unit = {
    components.foreach { component =>
      getHighlightFocusListeners(component).foreach { focusListener =>
        focusListener.asInstanceOf[HighlightingFocusListener].defocus()
        component.removeFocusListener(focusListener)
      }
    }
  }

  private def highlightComponentFocusListener(componentToHighlight: Component) = {
    new HighlightingFocusListener(componentToHighlight)
  }

  class HighlightingFocusListener(componentToHighlight: Component) extends FocusListener {
    override def focusGained(e: FocusEvent): Unit = {
      componentToHighlight.setBackground(ColorUtils.lighten(highlightBackgroundColor).by(10))
    }

    override def focusLost(e: FocusEvent): Unit = {
      defocus()
    }

    def defocus(): Unit = {
      componentToHighlight.setBackground(highlightBackgroundColor)
    }
  }

  def highlightWhenComponentReceivesFocus(otherComponent: Component): Unit = {
    otherComponent.addFocusListener(highlightThisComponentOnFocusListener)
  }
}








