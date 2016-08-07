package com.seismic.ui.swing

import java.awt.{Color, Component, Container, Dimension}
import javax.swing.{JLabel, JPanel, JTextField}

import com.daveclay.swing.util.Position._
import com.seismic.Instrument
import com.seismic.ui.swing.SwingThreadHelper.invokeLater

class InstrumentsUI(labelValue: String,
                    onAddInstrumentClicked: () => Unit,
                    onDeleteInstrumentClicked: (Instrument) => Unit,
                    onInstrumentUpdated: () => Unit,
                    size: Dimension,
                    backgroundColor: Color) extends JPanel {

  setPreferredSize(size)
  setOpaque(false)

  val label = SwingComponents.label(labelValue.toUpperCase, SwingComponents.monoFont18)
  label.setOpaque(false)
  position(label).atOrigin().in(this)

  val addInstrumentButton = SwingComponents.button("Add")
  addInstrumentButton.addActionListener(e => {
    onAddInstrumentClicked()
  })

  var instrumentNoteUIsOpt: Option[Seq[InstrumentUI]] = None

  def setInstruments(instruments: Seq[Instrument]): Unit = {
    removeCurrentInstrumentNoteUIs()
    instrumentNoteUIsOpt = Option(buildInstrumentNoteUIs(instruments))
    positionInstrumentUIs()
  }

  private def positionInstrumentUIs() {
    instrumentNoteUIsOpt.foreach { instrumentUIs =>
      addInstrumentUIs(label, instrumentUIs)
      position(addInstrumentButton).below(instrumentUIs.last).withMargin(4).in(this)
    }
    repaint()
  }

  def triggerOn(): Unit = {
    setBackground(new Color(170, 170, 170))
    setForeground(Color.BLACK)
  }

  def triggerOff(): Unit = {
    setBackground(backgroundColor)
  }

  def addInstrumentUIs(topComponent: Container, instrumentUIs: Seq[InstrumentUI]): Unit = {
    instrumentUIs.foldLeft(topComponent) { (previousComponent, instrumentUI) =>
      position(instrumentUI).below(previousComponent).withMargin(4).in(this)
      instrumentUI
    }
  }

  def buildInstrumentNoteUIs(instruments: Seq[Instrument]) = {
    instruments.map { instrument =>

      val deleteInstrument = () => {
        onDeleteInstrumentClicked(instrument)
      }

      new InstrumentUI(instrument,
                        onInstrumentUpdated,
                            deleteInstrument,
                            new Dimension(getPreferredSize.width, 20),
                            backgroundColor)
    }
  }

  def removeCurrentInstrumentNoteUIs(): Unit = {
    instrumentNoteUIsOpt.foreach { kickInstrumentUIs => removeInstrumentUIs(kickInstrumentUIs) }
  }

  def removeInstrumentUIs(instrumentNoteUIs: Seq[InstrumentUI]): Unit = {
    instrumentNoteUIs.foreach { instrumentNoteUI =>
      instrumentNoteUI.getFocusListeners.foreach { l => instrumentNoteUI.removeFocusListener(l) }
      remove(instrumentNoteUI)
    }
  }

  override def setBackground(color: Color): Unit = {
    super.setBackground(color)
    getLabels.foreach { label => label.setBackground(color) }
  }

  override def grabFocus(): Unit = {
    instrumentNoteUIsOpt.foreach { instrumentNoteUIs => instrumentNoteUIs.head.noteField.grabFocus() }
  }

  def getLabels: Seq[JLabel] = {
    instrumentNoteUIsOpt match {
      case Some(instrumentNoteUIs) =>
        instrumentNoteUIs.map { instrumentNoteUI =>
          instrumentNoteUI.noteField.label
        }
      case None => Array.empty[JLabel]
      case null => Array.empty[JLabel]
    }
  }

  def getFocusableFields: Seq[Component] = {
    instrumentNoteUIsOpt match {
      case Some(instrumentNoteUIs) =>
        instrumentNoteUIs.flatMap { instrumentNoteUI =>
          Array(instrumentNoteUI.noteField.inputField, instrumentNoteUI.deleteButton)
        }
      case None => Array.empty[Component]
    }
  }
}

class InstrumentUI(instrument: Instrument,
                   instrumentWasUpdated: () => Unit,
                   onDeleteInstrument: () => Unit,
                   size: Dimension,
                   backgroundColor: Color) extends JPanel {

  setPreferredSize(size)
  setOpaque(false)

  val noteField = new LabeledTextField("Note", 10, onValueChange)
  noteField.setText(instrument.notes.mkString(", "))

  val deleteButton = SwingComponents.button("DELETE")
  deleteButton.addActionListener(e => onDeleteInstrument())

  position(noteField).atOrigin().in(this)
  position(deleteButton).toTheRightOf(noteField).withMargin(10).in(this)

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





