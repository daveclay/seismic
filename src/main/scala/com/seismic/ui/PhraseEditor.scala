package com.seismic.ui

import java.awt.event.{ActionEvent, KeyListener}
import java.awt.{Color, Dimension, Insets}
import javax.swing.{BorderFactory, JPanel}

import com.daveclay.swing.util.Position._
import com.seismic.scala.ActionListenerExtensions._
import com.seismic.ui.utils.layout.GridBagLayoutHelper
import com.seismic.ui.utils.{HighlightOnFocus, LabeledTextField, SwingComponents}
import com.seismic.{Instrument, InstrumentBank, Phrase}

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
  val kickInstrumentBank = new InstrumentBankUI("KICK", onSongUpdated, instrumentUISize, backgroundColor)
  val altKickInstrumentBank = new InstrumentBankUI("alt-KICK", onSongUpdated, instrumentUISize, backgroundColor)
  val snareInstrumentBank = new InstrumentBankUI("SNARE", onSongUpdated, instrumentUISize, backgroundColor)
  val altSnareInstrumentBank = new InstrumentBankUI("alt-SNARE", onSongUpdated, instrumentUISize, backgroundColor)

  val onNameChange = (name: String) => curentPhraseOpt.foreach { phrase =>
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
    private val dimension = new Dimension(instrumentUISize.width * 2, instrumentUISize.height * 2)
    setPreferredSize(dimension)
    setMinimumSize(dimension)
    setOpaque(false)

    val helper = new GridBagLayoutHelper(this)

    val pad = new Insets(4, 4, 0, 0)
    helper.position(kickInstrumentBank).withPadding(0).fill().weightX(.5f).weightY(.5f).alignLeft().atOrigin().inParent()
    helper.position(altKickInstrumentBank).below(kickInstrumentBank).withPadding(0).fill().weightX(.5f).weightY(.5f).alignLeft().inParent()
    helper.position(snareInstrumentBank).nextTo(kickInstrumentBank).fill().weightX(.5f).weightY(1).alignLeft().inParent()
    helper.position(altSnareInstrumentBank).below(snareInstrumentBank).fill().weightX(.5f).weightY(1).alignLeft().inParent()
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

    kickInstrumentBank.setInstrumentBank(phrase.getInstrumentBanks("KICK"))
    altKickInstrumentBank.setInstrumentBank(phrase.getInstrumentBanks("ALTKICK"))
    snareInstrumentBank.setInstrumentBank(phrase.getInstrumentBanks("SNARE"))
    altSnareInstrumentBank.setInstrumentBank(phrase.getInstrumentBanks("ALTSNARE"))

    configureHighlighting()
  }

  private def configureHighlighting(): Unit = {
    highlight(this).onFocusOf(nameField,
                               patchField.inputField)
  }

  override def grabFocus(): Unit = {
    nameField.grabFocus()
  }

  override def addKeyListener(keyListener: KeyListener): Unit = {
    super.addKeyListener(keyListener)
    nameField.addKeyListener(keyListener)
    patchField.addKeyListener(keyListener)
  }
}
