package com.seismic.ui.swing

import java.awt.event.KeyListener
import java.awt.{Color, Dimension}
import javax.swing.JPanel

import com.daveclay.swing.util.Position._
import com.seismic.{Instrument, Phrase}

class PhraseEditor(onSongUpdated: () => Unit,
                   onPhraseUpdated: (Phrase) => Unit,
                   onDeletePhrase: (Phrase) => Unit,
                   size: Dimension,
                   val backgroundColor: Color) extends JPanel with HighlightOnFocus {

  SwingComponents.addBorder(this)
  setPreferredSize(size)
  setBackground(backgroundColor)

  var curentPhraseOpt: Option[Phrase] = None
  val instrumentUISize = new Dimension(200, 300)

  val kickInstrumentUI = new InstrumentsUI("Kick",
                                            onAddKickInstrumentClicked,
                                            onDeleteKickInstrumentClicked,
                                            onInstrumentUpdated,
                                            instrumentUISize,
                                            backgroundColor)

  val snareInstrumentUI = new InstrumentsUI("Snare",
                                             onAddSnareInstrumentClicked,
                                             onDeleteSnareInstrumentClicked,
                                             onInstrumentUpdated,
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

  val deleteButton = SwingComponents.button("DELETE")
  deleteButton.addActionListener(e => {
    curentPhraseOpt.foreach { phrase => onDeletePhrase(phrase) }
  })

  val phraseEditPanel = new JPanel() {
    setPreferredSize(new Dimension(PhraseEditor.this.size.width - 8, 30))
    setOpaque(false)

    position(label).atOrigin().in(this)
    position(nameField).toTheRightOf(label).withMargin(10).in(this)
    position(patchField).toTheRightOf(nameField).withMargin(10).in(this)
    position(deleteButton).toTheRightOf(patchField).withMargin(10).in(this)
  }

  position(phraseEditPanel).at(4, 4).in(this)

  def setPhrase(phrase: Phrase): Unit = {
    curentPhraseOpt = Option(phrase)
    nameField.setText(phrase.name)
    patchField.setText(phrase.patch.toString)
    kickInstrumentUI.setInstruments(phrase.kickInstruments)
    snareInstrumentUI.setInstruments(phrase.snareInstruments)

    position(kickInstrumentUI).below(phraseEditPanel).withMargin(10).in(this)
    position(snareInstrumentUI).toTheRightOf(kickInstrumentUI).withMargin(4).in(this)
    configureHighlighting()
  }

  private def configureHighlighting(): Unit = {
    highlight(this).onFocusOf(nameField,
                               patchField.inputField,
                               kickInstrumentUI.addInstrumentButton,
                               snareInstrumentUI.addInstrumentButton)
    .andFocusOf(kickInstrumentUI.getFocusableFields)
    .andFocusOf(snareInstrumentUI.getFocusableFields)
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
      kickInstrumentUI.setInstruments(phrase.kickInstruments)
      configureHighlighting()
      onSongUpdated()
    }
  }

  private def onDeleteKickInstrumentClicked(instrument: Instrument): Unit = {
    curentPhraseOpt.foreach { phrase =>
      phrase.removeKickInstrument(instrument)
      kickInstrumentUI.setInstruments(phrase.kickInstruments)
      configureHighlighting()
      onSongUpdated()
    }
  }

  private def onAddSnareInstrumentClicked(): Unit = {
    curentPhraseOpt.foreach { phrase =>
      phrase.addNewSnareInstrument()
      snareInstrumentUI.setInstruments(phrase.snareInstruments)
      configureHighlighting()
      onSongUpdated()
    }
  }

  private def onDeleteSnareInstrumentClicked(instrument: Instrument): Unit = {
    curentPhraseOpt.foreach { phrase =>
      phrase.removeSnareInstrument(instrument)
      snareInstrumentUI.setInstruments(phrase.snareInstruments)
      configureHighlighting()
      onSongUpdated()
    }
  }

  private def onInstrumentUpdated(): Unit = onSongUpdated()
}
