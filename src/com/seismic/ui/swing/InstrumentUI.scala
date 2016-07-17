package com.seismic.ui.swing

import java.awt.{Color, Container, Dimension}
import javax.swing.{JLabel, JPanel, JTextField}

import com.daveclay.swing.util.Position._
import com.seismic.Instrument
import com.seismic.ui.swing.SwingThreadHelper.invokeLater

import scala.collection.mutable.ArrayBuffer

class InstrumentUI(labelValue: String,
                   onAddInstrumentClicked: () => Unit,
                   onSongUpdated: () => Unit,
                   size: Dimension,
                   backgroundColor: Color) extends JPanel {

  setPreferredSize(size)
  setOpaque(false)

  val label = SwingComponents.label(labelValue)
  label.setBackground(backgroundColor)
  label.setForeground(new Color(200, 200, 200))
  position(label).atOrigin().in(this)

  val addInstrumentButton = SwingComponents.button("Add")
  addInstrumentButton.addActionListener(e => {
    onAddInstrumentClicked()
  })

  var instrumentNoteUIsOpt: Option[Seq[InstrumentNoteUI]] = None

  def setInstrumentNotes(instruments: Seq[Instrument]): Unit = {
    removeCurrentInstrumentNoteUIs()
    instrumentNoteUIsOpt = Option(buildInstrumentNoteUIs(instruments))
    positionInstrumentUIs()
  }

  def positionInstrumentUIs() {
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

  def addInstrumentUIs(topComponent: Container, instrumentUIs: Seq[InstrumentNoteUI]): Unit = {
    instrumentUIs.foldLeft(topComponent) { (previousComponent, instrumentUI) =>
      position(instrumentUI).below(previousComponent).withMargin(4).in(this)
      instrumentUI
    }
  }

  def buildInstrumentNoteUIs(instruments: Seq[Instrument]) = {
    instruments.map { instrument =>
      new InstrumentNoteUI(instrument,
                            onSongUpdated,
                            new Dimension(getPreferredSize.width, 20),
                            backgroundColor)
    }
  }

  def removeCurrentInstrumentNoteUIs(): Unit = {
    instrumentNoteUIsOpt.foreach { kickInstrumentUIs => removeInstrumentUIs(kickInstrumentUIs) }
  }

  def removeInstrumentUIs(instrumentNoteUIs: Seq[InstrumentNoteUI]): Unit = {
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
    instrumentNoteUIsOpt.foreach { instrumentNoteUIs => instrumentNoteUIs.head.nameField.grabFocus() }
  }

  def getLabels: Seq[JLabel] = {
    instrumentNoteUIsOpt match {
      case Some(instrumentNoteUIs) =>
        instrumentNoteUIs.map { instrumentNoteUI =>
          instrumentNoteUI.nameField.label
        }
      case None => Array.empty[JLabel]
      case null => Array.empty[JLabel]
    }
  }

  def getInputFields: Seq[JTextField] = {
    instrumentNoteUIsOpt match {
      case Some(instrumentNoteUIs) =>
        instrumentNoteUIs.map { instrumentNoteUI =>
          instrumentNoteUI.nameField.inputField
        }
      case None => Array.empty[JTextField]
    }
  }
}

class InstrumentNoteUI(instrument: Instrument,
                       onSongUpdated: () => Unit,
                       size: Dimension,
                       backgroundColor: Color) extends JPanel {

  setPreferredSize(size)
  setOpaque(false)

  val nameField = new LabeledTextField("Note", 10, onValueChange)
  nameField.setText(instrument.notes.mkString(", "))

  position(nameField).atOrigin().in(this)

  instrument.wasTriggeredOn { (pitch) =>
    invokeLater { () => nameField.highlightField() }
  }

  instrument.wasTriggeredOff { () =>
    invokeLater { () => nameField.unhighlightField() }
  }

  private def onValueChange(value: String):Unit = {
    instrument.setNotes(value.split(", "))
    onSongUpdated()
  }
}





