package com.seismic.ui.swing

import java.awt._
import javax.swing._
import javax.swing.filechooser.FileNameExtensionFilter

import com.daveclay.swing.util.Position.position
import com.daveclay.swing.util.Size.setPreferredSize
import com.seismic._
import com.seismic.messages._
import com.seismic.ui.swing.SwingThreadHelper.invokeLater
import collection.mutable.ArrayBuffer

class SeismicUIFactory {
  var seismicUIOpt: Option[SeismicUI] = None

  def build(seismic: Seismic) = {
    val frame = new JFrame("Seismic")
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.pack()

    val seismicUI = new SeismicUI(seismic, frame, frame.getGraphics)
    frame.pack()
    frame.setVisible(true)

    seismicUIOpt = Some(seismicUI)

    seismicUI
  }

  def handleMessage(message: Message): Unit = {
    seismicUIOpt match {
      case Some(seismicUI) =>
        invokeLater { () =>
          seismicUI.handleMessage(message)
        }
      case None => System.out.println("UI Not loaded yet...")
    }
  }
}

class SeismicUI(seismic: Seismic,
                frame: JFrame,
                graphics: Graphics) {

  val backgroundColor = new Color(50, 50, 60)
  val mainPanel = frame.getContentPane

  // TODO: build a factory that knows about all this shared styling
  // then ask the factory to create components, the factory sets the styling
  // remove all the styling from this layout and state handling.

  val titleFont = new Font("Arial", Font.PLAIN, 23)
  val monoFont = new Font("PT Mono", Font.PLAIN, 11)
  val title = new JLabel("SEISMIC")
  val kickMonitor = new Meter("KICK", monoFont, new Dimension(300, 30))
  val snareMonitor = new Meter("SNARE", monoFont, new Dimension(300, 30))
  val triggerMonitors = Map(
    "KICK" -> kickMonitor,
    "SNARE" -> snareMonitor)

  val handleMeter = new HandleMeter(monoFont, new Dimension(120, 120), graphics)
  handleMeter.setBackground(backgroundColor)

  val setlistUI = new SetlistUI(new Dimension(800, 500), backgroundColor)

  val openSetListButton = new JButton("Open Set List")
  openSetListButton.addActionListener(e => {
    showOpenSetListFileChooser()
  })

  val newSetListButton = new JButton("New Set List")
  newSetListButton.addActionListener(e => {
    setlistUI.setSetList(seismic.getEmptySetList)
  })

  setlistUI.setBackground(backgroundColor)

  setPreferredSize(frame, 800, 600)
  mainPanel.setBackground(backgroundColor)

  title.setFont(titleFont)
  title.setForeground(new Color(200, 200, 210))

  position(title).at(4, 4).in(mainPanel)
  position(openSetListButton).toTheRightOf(title).withMargin(5).in(mainPanel)
  position(newSetListButton).toTheRightOf(openSetListButton).withMargin(5).in(mainPanel)
  position(kickMonitor).below(title).withMargin(5).in(mainPanel)
  position(snareMonitor).toTheRightOf(kickMonitor).withMargin(5).in(mainPanel)
  position(handleMeter).toTheRightOf(snareMonitor).in(mainPanel)
  position(setlistUI).below(kickMonitor).withMargin(10).in(mainPanel)

  def showOpenSetListFileChooser(): Unit = {
    val chooser = new JFileChooser
    val filter = new FileNameExtensionFilter("JSON Files", "json")
    chooser.setFileFilter(filter)
    if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
      val file = chooser.getSelectedFile
      setlistUI.setSetList(seismic.openSetList(file))
    }
  }

  def handleMessage(message: Message): Unit = {
    invokeLater { () =>
      // System.out.println(Thread.currentThread().getName + " with message " + message)
      message match {
        case triggerOn: TriggerOnMessage => handleTriggerOn(triggerOn)
        case triggerOff: TriggerOffMessage => handleTriggerOff(triggerOff)
        case _ => System.out.println(f"Unknown message: $message")
      }
    }
  }

  private def handleTriggerOn(triggerOn: TriggerOnMessage): Unit = {
    triggerMonitors.get(triggerOn.name) match {
      case Some(monitor) =>
        val value = triggerOn.triggerValue
        val name = triggerOn.name
        monitor.setValue(value)
        handleMeter.setValue(triggerOn.handleValue)
        handleMeter.repaint()
        monitor.repaint()
      case None =>
    }
  }

  private def handleTriggerOff(triggerOff: TriggerOffMessage): Unit = {
    triggerMonitors.get(triggerOff.name) match {
      case Some(monitor) =>
        val name = triggerOff.name
        monitor.off()
        monitor.repaint()
      case None =>
    }
  }
}

class SetlistUI(size: Dimension,
                backgroundColor: Color) extends JPanel {
  setPreferredSize(size)
  setBackground(backgroundColor)

  var setListOpt: Option[SetList] = None
  val handleUpdate = () => setListOpt.foreach { setlist => setlist.write() }
  val currentSongUI = new SongUI(size, backgroundColor, handleUpdate)
  val onNameChange = (name: String) => setListOpt.foreach {
    setList => {
      setList.setName(name)
      handleUpdate()
    }
  }

  val nameField = new LabeledTextField("Set List", backgroundColor, 12, onNameChange)

  currentSongUI.setBackground(backgroundColor)
  position(nameField).atOrigin().in(this)
  position(currentSongUI).below(nameField).withMargin(4).in(this)

  def setSetList(setList: SetList): Unit = {
    setListOpt = Option(setList)
    nameField.setText(setList.name)
    currentSongUI.setSong(setList.songs(0))
  }
}

class SongUI(size: Dimension,
             backgroundColor: Color,
             onSongUpdated: () => Unit) extends JPanel {
  setPreferredSize(size)
  setBackground(backgroundColor)

  var songOpt: Option[Song] = None

  val onNameChange = (name: String) => songOpt.foreach {
    song => song.setName(name)
    onSongUpdated()
  }

  val onChannelChange = (channel: String) => songOpt.foreach {
    song => song.setChannel(channel.toInt)
    onSongUpdated()
  }

  val nameField = new LabeledTextField("Song", backgroundColor, 12, onNameChange)
  val channelField = new LabeledTextField("MIDI Channel", backgroundColor, 3, onChannelChange)
  val phraseUI = new PhraseUI(onSongUpdated, new Dimension(size.width, size.height), backgroundColor)

  // TODO: how do I add more phrases to the song?

  position(nameField).atOrigin().in(this)
  position(channelField).toTheRightOf(nameField).withMargin(5).in(this)
  position(phraseUI).below(nameField).withMargin(5).in(this)

  def setSong(song: Song): Unit = {
    this.songOpt = Option(song)

    nameField.setText(song.name)
    channelField.setText(song.channel.toString)
    phraseUI.setPhrase(song.phrases(0))
  }
}

class PhraseUI(onSongUpdated: () => Unit,
               size: Dimension,
               backgroundColor: Color) extends JPanel {
  setPreferredSize(size)
  setBackground(backgroundColor)

  var curentPhraseOpt: Option[Phrase] = None
  val instrumentUISize = new Dimension(200, 300)

  val kickInstrumentUI = new InstrumentUI("Kick",
                                           onAddKickInstrumentClicked,
                                           onSongUpdated,
                                           instrumentUISize,
                                           backgroundColor)

  val snareInstrumentUI = new InstrumentUI("Snare",
                                            onAddSnareInstrumentClicked,
                                            onSongUpdated,
                                            instrumentUISize,
                                            backgroundColor)

  val onNameChange = (name: String) => curentPhraseOpt.foreach {
    phrase => phrase.setName(name)
    onSongUpdated()
  }

  val nameField = new LabeledTextField("Phrase", backgroundColor, 12, onNameChange)
  position(nameField).atOrigin().in(this)

  def setPhrase(phrase: Phrase): Unit = {
    curentPhraseOpt = Option(phrase)
    nameField.setText(phrase.name)
    kickInstrumentUI.setInstrumentNotes(phrase.kickInstruments)
    snareInstrumentUI.setInstrumentNotes(phrase.snareInstruments)
    position(kickInstrumentUI).below(nameField).withMargin(4).in(this)
    position(snareInstrumentUI).toTheRightOf(kickInstrumentUI).withMargin(4).in(this)
  }

  private def positionInstrumentUIs() {
    position(kickInstrumentUI).below(nameField).withMargin(4).in(this)
    position(snareInstrumentUI).below(kickInstrumentUI).withMargin(4).in(this)
  }

  private def onAddKickInstrumentClicked(): Unit = {
    curentPhraseOpt.foreach { phrase =>
      phrase.addNewKickInstrument()
      kickInstrumentUI.setInstrumentNotes(phrase.kickInstruments)
      onSongUpdated()
    }
  }

  private def onAddSnareInstrumentClicked(): Unit = {
    curentPhraseOpt.foreach { phrase =>
      phrase.addNewSnareInstrument()
      snareInstrumentUI.setInstrumentNotes(phrase.snareInstruments)
      onSongUpdated()
    }
  }
}

class InstrumentUI(labelValue: String,
                   onAddInstrumentClicked: () => Unit,
                   onSongUpdated: () => Unit,
                   size: Dimension,
                   backgroundColor: Color) extends JPanel {

  setPreferredSize(size)
  setBackground(backgroundColor)

  val label = SwingComponents.textField(backgroundColor, 7)
  label.setText(labelValue)
  position(label).atOrigin().in(this)

  val addInstrumentButton = new JButton("Add")
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
                            new Dimension(size.width, 20),
                            backgroundColor)
    }
  }

  def removeCurrentInstrumentNoteUIs(): Unit = {
    instrumentNoteUIsOpt.foreach { kickInstrumentUIs => removeInstrumentUIs(kickInstrumentUIs) }
  }

  def removeInstrumentUIs(instrumentUIs: Seq[InstrumentNoteUI]): Unit = {
    instrumentUIs.foreach { instrumentUI => remove(instrumentUI) }
  }
}

class InstrumentNoteUI(instrument: Instrument,
                       onSongUpdated: () => Unit,
                       size: Dimension,
                       backgroundColor: Color) extends JPanel {

  setPreferredSize(size)
  setBackground(backgroundColor)

  val onValueChange = (value: String) => {
    // TODO: value should be maybe "C2, C#2, D3" or "C2, 63, C#2"
    instrument.setNotes(value.split(", ").map { value => value.toInt }.to[ArrayBuffer])
    onSongUpdated()
  }

  val nameField = new LabeledTextField("Note", backgroundColor, 10, onValueChange)
  nameField.setText(instrument.notes.mkString(", "))
  position(nameField).atOrigin().in(this)

}


