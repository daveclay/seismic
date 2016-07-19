package com.seismic.ui.swing

import java.awt.event.KeyListener
import java.awt.{Color, Dimension}
import javax.swing.border.CompoundBorder
import javax.swing.{BorderFactory, JPanel}

import com.daveclay.swing.util.Position._
import com.seismic.Phrase

class PhraseEditor(onAddInstrumentClicked: () => Unit,
                   onPhraseUpdated: (Phrase) => Unit,
                   size: Dimension,
                   val backgroundColor: Color) extends JPanel with HighlightOnFocus {

  SwingComponents.addBorder(this)
  setPreferredSize(size)
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

  val onNameChange = (name: String) => curentPhraseOpt.foreach { phrase =>
    if ( ! phrase.name.equals(name)) {
      kickInstrumentUI.grabFocus()
    }
    phrase.name = name
    onPhraseUpdated(phrase)
  }

  val onPatchChange = (patch: String) => curentPhraseOpt.foreach { phrase =>
    phrase.patch = Integer.valueOf(patch)
    onPhraseUpdated(phrase)
  }

  val label = SwingComponents.label("PHRASE", SwingComponents.monoFont18)
  val nameField = SwingComponents.textField(Color.BLACK, 12, onNameChange)
  val patchField = new LabeledTextField("Patch", 3, onPatchChange)

  position(label).at(4, 4).in(this)
  position(nameField).toTheRightOf(label).withMargin(10).in(this)
  position(patchField).toTheRightOf(nameField).withMargin(4).in(this)

  def setPhrase(phrase: Phrase): Unit = {
    curentPhraseOpt = Option(phrase)
    nameField.setText(phrase.name)
    patchField.setText(phrase.patch.toString)
    kickInstrumentUI.setInstrumentNotes(phrase.kickInstruments)
    snareInstrumentUI.setInstrumentNotes(phrase.snareInstruments)
    position(kickInstrumentUI).below(nameField).withMargin(10).in(this)
    position(snareInstrumentUI).toTheRightOf(kickInstrumentUI).withMargin(4).in(this)

    highlight(this, nameField).onFocusOf(nameField,
                                          kickInstrumentUI.addInstrumentButton,
                                          snareInstrumentUI.addInstrumentButton)
    .andFocusOf(kickInstrumentUI.getInputFields)
    .andFocusOf(snareInstrumentUI.getInputFields)
  }

  override def grabFocus(): Unit = {
    nameField.grabFocus()
  }

  override def addKeyListener(keyListener: KeyListener): Unit = {
    super.addKeyListener(keyListener)
    nameField.addKeyListener(keyListener)
    patchField.addKeyListener(keyListener)
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
