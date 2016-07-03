package com.seismic.ui.swing

import java.awt.{Color, Dimension}
import javax.swing.border.CompoundBorder
import javax.swing.{BorderFactory, JPanel}

import com.daveclay.swing.util.Position._
import com.seismic.Phrase

class PhraseEditor(onAddInstrumentClicked: () => Unit,
                   onPhraseUpdated: (Phrase) => Unit,
                   val backgroundColor: Color) extends JPanel with HighlightOnFocus {

  SwingComponents.addBorder(this)
  setPreferredSize(new Dimension(400, 400))
  setBackground(backgroundColor)

  var curentPhraseOpt: Option[Phrase] = None
  val instrumentUISize = new Dimension(200, 300)

  val kickInstrumentUI = new InstrumentUI("Kick",
                                           onAddKickInstrumentClicked,
                                           onAddInstrumentClicked,
                                           instrumentUISize,
                                           backgroundColor)

  val snareInstrumentUI = new InstrumentUI("Snare",
                                            onAddSnareInstrumentClicked,
                                            onAddInstrumentClicked,
                                            instrumentUISize,
                                            backgroundColor)

  val onNameChange = (name: String) => curentPhraseOpt.foreach {
    phrase => {
      if ( ! phrase.name.equals(name)) {
        kickInstrumentUI.grabFocus()
      }
      phrase.setName(name)
      onPhraseUpdated(phrase)
    }
  }

  val nameField = new LabeledTextField("Phrase", backgroundColor, 12, onNameChange)
  position(nameField).at(4, 4).in(this)

  def setPhrase(phrase: Phrase): Unit = {
    curentPhraseOpt = Option(phrase)
    nameField.setText(phrase.name)
    kickInstrumentUI.setInstrumentNotes(phrase.kickInstruments)
    snareInstrumentUI.setInstrumentNotes(phrase.snareInstruments)
    position(kickInstrumentUI).below(nameField).withMargin(10).in(this)
    position(snareInstrumentUI).toTheRightOf(kickInstrumentUI).withMargin(4).in(this)

    highlight(this, nameField).onFocusOf(nameField.inputField,
                                          kickInstrumentUI.addInstrumentButton,
                                          snareInstrumentUI.addInstrumentButton)
    .andFocusOf(kickInstrumentUI.getInputFields)
    .andFocusOf(snareInstrumentUI.getInputFields)
  }

  override def grabFocus(): Unit = {
    nameField.grabFocus()
  }

  private def onAddKickInstrumentClicked(): Unit = {
    curentPhraseOpt.foreach { phrase =>
      phrase.addNewKickInstrument()
      kickInstrumentUI.setInstrumentNotes(phrase.kickInstruments)
      onAddInstrumentClicked()
    }
  }

  private def onAddSnareInstrumentClicked(): Unit = {
    curentPhraseOpt.foreach { phrase =>
      phrase.addNewSnareInstrument()
      snareInstrumentUI.setInstrumentNotes(phrase.snareInstruments)
      onAddInstrumentClicked()
    }
  }
}
