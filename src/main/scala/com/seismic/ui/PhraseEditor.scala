package com.seismic.ui

import java.awt.event.{ActionEvent, KeyListener}
import java.awt.{Color, Dimension, Insets}
import javax.swing.{BorderFactory, JPanel}

import com.daveclay.swing.util.Position._
import com.seismic.scala.ActionListenerExtensions._
import com.seismic.ui.utils.layout.GridBagLayoutHelper
import com.seismic.ui.utils.{HighlightOnFocus, LabeledTextField, SwingComponents}
import com.seismic.{Instrument, Phrase}

class PhraseEditor(onSongUpdated: () => Unit,
                   onPhraseUpdated: (Phrase) => Unit,
                   onDupPhrase: (Phrase) => Unit,
                   onDeletePhrase: (Phrase) => Unit,
                   size: Dimension,
                   val backgroundColor: Color) extends JPanel with HighlightOnFocus {

  SwingComponents.addBorder(this)
  setPreferredSize(size)
  setMinimumSize(size)
  setBackground(backgroundColor)

  var curentPhraseOpt: Option[Phrase] = None
  val instrumentUISize = new Dimension(242, 280)

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
  label.setMinimumSize(label.getPreferredSize)

  val nameField = SwingComponents.textField(Color.BLACK, 12, onNameChange)
  val patchField = new LabeledTextField("Patch", 3, onPatchChange)

  val dupButton = SwingComponents.button("Dup")
  dupButton.addActionListener((e: ActionEvent) => {
    curentPhraseOpt.foreach { phrase => onDupPhrase(phrase) }
  })

  val deleteButton = SwingComponents.deleteButton()
  deleteButton.addActionListener((e: ActionEvent) => {
    curentPhraseOpt.foreach { phrase => onDeletePhrase(phrase) }
  })

  val phraseEditPanel = new JPanel() {
    private val dimension = new Dimension(PhraseEditor.this.size.width - 8, 30)
    setPreferredSize(dimension)
    setMinimumSize(dimension)
    setOpaque(false)
    val helper = new GridBagLayoutHelper(this)

    val pad = new Insets(4, 4, 0, 0)

    helper.position(label).atOrigin().inParent()
    helper.position(nameField).nextTo(label).withPadding(10).fillHorizontal().weightX(1).inParent()
    helper.position(patchField).nextTo(nameField).withPadding(10).inParent()
    helper.position(dupButton).nextTo(patchField).withPadding(10).inParent()
    helper.position(deleteButton).nextTo(dupButton).withPadding(10).inParent()
    helper.horizontalSpacer().weightX(.1f).nextTo(deleteButton).inParent()
  }

  var instrumentsPanel = new JPanel() {
    private val dimension = new Dimension(instrumentUISize.width * 2, instrumentUISize.height)
    setPreferredSize(dimension)
    setMinimumSize(dimension)
    setOpaque(false)

    val helper = new GridBagLayoutHelper(this)

    val pad = new Insets(4, 4, 0, 0)
    helper.position(kickInstrumentUI).withPadding(10).fill().weightX(.5f).weightY(1).alignLeft().atOrigin().inParent()
    helper.position(snareInstrumentUI).nextTo(kickInstrumentUI).fill().weightX(.5f).weightY(1).alignLeft().inParent()
  }

  val instructions = SwingComponents.label("<html><b>Prefixes:</b><br/>" +
    "X: do not send note off when released.<br/>" +
    "N: send note off when triggered.<br/>" +
    "T: send note off when released.<br/>" +
    "<b>Suffixes:</b><br/>" +
    "/4: send note message on midi channel 4")

  val instructionsSize = new Dimension(300, 100)
  instructions.setPreferredSize(instructionsSize)
  instructions.setMinimumSize(instructionsSize)

  val helper = new GridBagLayoutHelper(this)

  val pad = new Insets(4, 4, 0, 0)
  helper.position(phraseEditPanel).withPadding(4).fillHorizontal().weightX(1).alignLeft().atOrigin().inParent()
  helper.position(instrumentsPanel).below(phraseEditPanel).fill().weightY(1).weightX(1).alignLeft().inParent()
  helper.position(instructions).below(instrumentsPanel).withPadding(pad).fillHorizontal().weightX(1).alignLeft().inParent()

  def highlightBackgroundColor = backgroundColor

  def setPhrase(phrase: Phrase): Unit = {
    curentPhraseOpt = Option(phrase)
    nameField.setText(phrase.name)
    patchField.setText(phrase.patch.toString)
    kickInstrumentUI.setInstruments(phrase.getKickInstruments)
    snareInstrumentUI.setInstruments(phrase.getSnareInstruments)

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
      kickInstrumentUI.setInstruments(phrase.getKickInstruments)
      configureHighlighting()
      onSongUpdated()
    }
  }

  private def onDeleteKickInstrumentClicked(instrument: Instrument): Unit = {
    curentPhraseOpt.foreach { phrase =>
      phrase.removeKickInstrument(instrument)
      kickInstrumentUI.setInstruments(phrase.getKickInstruments)
      configureHighlighting()
      onSongUpdated()
    }
  }

  private def onAddSnareInstrumentClicked(): Unit = {
    curentPhraseOpt.foreach { phrase =>
      phrase.addNewSnareInstrument()
      snareInstrumentUI.setInstruments(phrase.getSnareInstruments)
      configureHighlighting()
      onSongUpdated()
    }
  }

  private def onDeleteSnareInstrumentClicked(instrument: Instrument): Unit = {
    curentPhraseOpt.foreach { phrase =>
      phrase.removeSnareInstrument(instrument)
      snareInstrumentUI.setInstruments(phrase.getSnareInstruments)
      configureHighlighting()
      onSongUpdated()
    }
  }

  private def onInstrumentUpdated(): Unit = onSongUpdated()
}
