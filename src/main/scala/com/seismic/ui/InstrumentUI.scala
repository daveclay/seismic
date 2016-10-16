package com.seismic.ui

import java.awt.event.ActionEvent
import java.awt.{Color, Dimension}
import javax.swing.{BorderFactory, JPanel}

import com.seismic.Instrument
import com.seismic.ui.utils.SwingThreadHelper._
import com.seismic.ui.utils.layout.GridBagLayoutHelper
import com.seismic.ui.utils.{LabeledTextField, SwingComponents}
import com.seismic.scala.ActionListenerExtensions._

class InstrumentUI(instrument: Instrument,
                   instrumentWasUpdated: () => Unit,
                   onDeleteInstrument: () => Unit,
                   size: Dimension,
                   backgroundColor: Color) extends JPanel {

  setPreferredSize(size)
  setMinimumSize(size)
  setOpaque(false)

  val noteField = new LabeledTextField("Note", 24, onValueChange)
  noteField.setText(instrument.notes.mkString(", "))

  val deleteButton = SwingComponents.deleteButton()
  deleteButton.addActionListener((e: ActionEvent) => onDeleteInstrument())

  val helper = new GridBagLayoutHelper(this)
  helper.position(noteField).atOrigin().fillHorizontal().weightX(1).inParent()
  helper.position(deleteButton).nextTo(noteField).withPadding(6).inParent()

  instrument.wasTriggeredOn { (pitch) =>
    invokeLater { () => noteField.highlightField() }
  }

  instrument.wasTriggeredOff { () =>
    invokeLater { () => noteField.unhighlightField() }
  }

  private def onValueChange(value: String):Unit = {
    instrument.setNotes(value.split(",").map { s => s.trim() })
    instrumentWasUpdated()
  }
}
