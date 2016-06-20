package com.seismic.ui.swing

import java.awt._
import java.awt.event._
import javax.swing._
import javax.swing.filechooser.FileNameExtensionFilter

import com.daveclay.swing.util.Position.position
import com.daveclay.swing.util.Size.setPreferredSize
import com.seismic._
import com.seismic.messages._
import com.seismic.ui.swing.SwingThreadHelper.invokeLater

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
  openSetListButton.addActionListener((e: ActionEvent) => {
    showOpenSetListFileChooser()
  })

  setlistUI.setBackground(backgroundColor)

  setPreferredSize(frame, 800, 600)
  mainPanel.setBackground(backgroundColor)

  title.setFont(titleFont)
  title.setForeground(new Color(200, 200, 210))

  position(title).at(4, 4).in(mainPanel)
  position(openSetListButton).toTheRightOf(title).withMargin(5).in(mainPanel)
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

  currentSongUI.setBackground(backgroundColor)
  position(currentSongUI).atOrigin().in(this)

  def setSetList(setList: SetList): Unit = {
    setListOpt = Option(setList)
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

  val nameField = new LabeledTextField("Name", backgroundColor, 12, onNameChange)
  val channelField = new LabeledTextField("MIDI Channel", backgroundColor, 3, onChannelChange)
  val phraseUI = new PhraseUI(onSongUpdated, new Dimension(size.width, size.height), backgroundColor)

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
  var instrumentUIOpts: Option[Array[InstrumentUI]] = None
  val kickLabel = SwingComponents.textField(backgroundColor, 7)
  kickLabel.setText("Kick")
  val snareLabel = SwingComponents.textField(backgroundColor, 7)
  snareLabel.setText("Snare")

  val onNameChange = (name: String) => curentPhraseOpt.foreach {
    phrase => phrase.setName(name)
    onSongUpdated()
  }

  val nameField = new LabeledTextField("Name", backgroundColor, 12, onNameChange)
  position(nameField).atOrigin().in(this)

  def setPhrase(phrase: Phrase): Unit = {
    curentPhraseOpt = Option(phrase)
    nameField.setText(phrase.name)
    clearInstruments()
    val kickInstrumentUIs = buildInstrumentUIs(phrase.kickInstruments)
    val snareInstrumentUIs = buildInstrumentUIs(phrase.snareInstruments)
    instrumentUIOpts = Option(kickInstrumentUIs ++ snareInstrumentUIs)
    position(kickLabel).below(nameField).withMargin(4).in(this)
    addInstrumentUIs(kickLabel, kickInstrumentUIs)
    position(snareLabel).below(kickInstrumentUIs.last).withMargin(4).in(this)
    addInstrumentUIs(snareLabel, snareInstrumentUIs)
  }

  def addInstrumentUIs(topComponent: Container, instrumentUIs: Array[InstrumentUI]): Unit = {
    instrumentUIs.foldLeft(topComponent) { (previousComponent, instrumentUI) =>
      position(instrumentUI).below(previousComponent).withMargin(4).in(this)
      instrumentUI
    }
  }

  def buildInstrumentUIs(instruments: Array[Instrument]) = {
    instruments.map { instrument =>
      new InstrumentUI(instrument,
                        onSongUpdated,
                        new Dimension(size.width, 20),
                        backgroundColor)
    }
  }

  def clearInstruments(): Unit = {
    remove(kickLabel)
    remove(snareLabel)
    instrumentUIOpts match {
      case Some(instrumentUIs) => instrumentUIs.foreach { instrumentUI => remove(instrumentUI) }
      case None =>
    }
  }
}

class InstrumentUI(instrument: Instrument,
                   onSongUpdated: () => Unit,
                   size: Dimension,
                   backgroundColor: Color) extends JPanel {

  setPreferredSize(size)
  setBackground(backgroundColor)

  val onValueChange = (value: String) => {
    // TODO: value should be maybe "C2, C#2, D3" or "C2, 63, C#2"
    instrument.setNotes(value.split(", ").map { value => value.toInt })
    onSongUpdated()
  }

  val nameField = new LabeledTextField("Note", backgroundColor, 10, onValueChange)
  nameField.setText(instrument.notes.mkString(", "))
  position(nameField).atOrigin().in(this)

}


