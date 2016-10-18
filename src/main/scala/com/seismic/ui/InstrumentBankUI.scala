package com.seismic.ui

import java.awt.{Color, Dimension}
import javax.swing.{BorderFactory, JPanel}

import com.seismic.ui.utils.HighlightOnFocus
import com.seismic.ui.utils.layout.GridBagLayoutHelper
import com.seismic.{Instrument, InstrumentBank}

class InstrumentBankUI(name: String,
                       onSongUpdated: () => Unit,
                       instrumentUISize: Dimension,
                       backgroundColor: Color) extends JPanel with HighlightOnFocus {

  setOpaque(false)

  var currentInstrumentBankOpt: Option[InstrumentBank] = None

  val instrumentsUI = new InstrumentsUI(name,
                                         onAddInstrumentClicked,
                                         onDeleteInstrumentClicked,
                                         onInstrumentUpdated,
                                         instrumentUISize,
                                         backgroundColor)

  val helper = new GridBagLayoutHelper(this)
  helper.position(instrumentsUI).fill().weightX(1).weightY(1).atOrigin().inParent()

  def setInstrumentBank(instrumentBank: InstrumentBank): Unit = {
    currentInstrumentBankOpt = Option(instrumentBank)
    instrumentsUI.setInstruments(instrumentBank.instruments)
    configureHighlighting()
  }

  def highlightBackgroundColor = backgroundColor

  private def onInstrumentUpdated(): Unit = onSongUpdated()

  private def onAddInstrumentClicked(): Unit = {
    currentInstrumentBankOpt.foreach { instrumentBank =>
      instrumentBank.addNewInstrument()
      handleInstrumentsUpdated()
    }
  }

  private def onDeleteInstrumentClicked(instrument: Instrument): Unit = {
    currentInstrumentBankOpt.foreach { instrumentBank =>
      instrumentBank.removeInstrument(instrument)
      handleInstrumentsUpdated()
    }
  }

  private def handleInstrumentsUpdated(): Unit = {
    currentInstrumentBankOpt.foreach { instrumentBank =>
      instrumentsUI.setInstruments(instrumentBank.instruments)
      configureHighlighting()
      onSongUpdated()
    }
  }

  private def configureHighlighting(): Unit = {
    highlight(this)
      .onFocusOf(instrumentsUI.addInstrumentButton)
      .andFocusOf(instrumentsUI.getFocusableFields)
  }
}
