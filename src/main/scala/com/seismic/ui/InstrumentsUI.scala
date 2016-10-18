package com.seismic.ui

import java.awt.event.ActionEvent
import java.awt._
import javax.swing.{BorderFactory, JLabel, JPanel}

import com.daveclay.swing.util.Position._
import com.seismic.Instrument
import com.seismic.scala.ActionListenerExtensions._
import com.seismic.ui.utils.SwingThreadHelper.invokeLater
import com.seismic.ui.utils.layout.GridBagLayoutHelper
import com.seismic.ui.utils.{LabeledTextField, SwingComponents}

class InstrumentsUI(labelValue: String,
                    onAddInstrumentClicked: () => Unit,
                    onDeleteInstrumentClicked: (Instrument) => Unit,
                    onInstrumentUpdated: () => Unit,
                    size: Dimension,
                    backgroundColor: Color) extends JPanel {

  setPreferredSize(size)
  setMinimumSize(size)
  setOpaque(false)

  val label = SwingComponents.label(labelValue.toUpperCase, SwingComponents.monoFont18)
  label.setOpaque(false)

  val spacer = new JPanel()

  val helper = new GridBagLayoutHelper(this)
  helper.position(label).atOrigin().align(GridBagConstraints.NORTHWEST).inParent()

  val addInstrumentButton = SwingComponents.button("Add")
  addInstrumentButton.addActionListener((e: ActionEvent) => onAddInstrumentClicked())

  var instrumentNoteUIsOpt: Option[Seq[InstrumentUI]] = None

  def setInstruments(instruments: Seq[Instrument]): Unit = {
    removeCurrentInstrumentNoteUIs()
    instrumentNoteUIsOpt = Option(buildInstrumentNoteUIs(instruments))
    positionInstrumentUIs()
  }

  private def positionInstrumentUIs() {
    instrumentNoteUIsOpt.foreach { instrumentUIs =>
      addInstrumentUIs(label, instrumentUIs)
      helper.position(addInstrumentButton).below(instrumentUIs.last).alignLeft().withPadding(4).inParent()
      remove(spacer)
      helper.verticalSpacer(spacer).below(addInstrumentButton).inParent()
    }
    revalidate()
    repaint()
  }

  def triggerOn(): Unit = {
    setBackground(new Color(170, 170, 170))
    setForeground(Color.BLACK)
  }

  def triggerOff(): Unit = {
    setBackground(backgroundColor)
  }

  private def addInstrumentUIs(topComponent: Container, instrumentUIs: Seq[InstrumentUI]): Unit = {
    instrumentUIs.foldLeft(topComponent) { (previousComponent, instrumentUI) =>
      helper.position(instrumentUI).below(previousComponent).fillHorizontal().weightX(1).inParent()
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
                            new Dimension(getPreferredSize.width, 30),
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







