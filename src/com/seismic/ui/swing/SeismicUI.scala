package com.seismic.ui.swing

import java.awt._
import java.awt.event._
import java.io.File
import javax.swing._

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

    System.setProperty("apple.laf.useScreenMenuBar", "true")
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)

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
  val title = SwingComponents.label("SEISMIC")

  mainPanel.setBackground(backgroundColor)
  mainPanel.setFocusTraversalPolicy(new ContainerOrderFocusTraversalPolicy)

  val kickMonitor = new Meter("KICK", monoFont, new Dimension(300, 30))
  val snareMonitor = new Meter("SNARE", monoFont, new Dimension(300, 30))
  val triggerMonitors = Map(
    "KICK" -> kickMonitor,
    "SNARE" -> snareMonitor)

  val handleMeter = new HandleMeter(monoFont, new Dimension(120, 120), graphics)
  handleMeter.setBackground(backgroundColor)

  val setlistUI = new SetlistUI(new Dimension(800, 500), backgroundColor)

  setPreferredSize(frame, 800, 600)
  setlistUI.setBackground(backgroundColor)

  title.setFont(titleFont)
  title.setForeground(new Color(200, 200, 210))

  val onFileSelected = (file: File) => setlistUI.setSetList(seismic.openSetList(file))
  val fileChooser = new JSONFileChooser(frame, onFileSelected)

  val newSetList = () => setlistUI.setSetList(seismic.getEmptySetList)
  val saveSetList = () => setlistUI.save()
  val openSetList = () => fileChooser.show()

  val menuBar = new JMenuBar
  val fileMenu = new SMenu("File")
  menuBar.add(fileMenu)

  fileMenu.addItem("New Set List", acceleratorMnemonicKey = KeyEvent.VK_N, newSetList)
  fileMenu.addItem("Open", acceleratorMnemonicKey = KeyEvent.VK_O, openSetList)
  fileMenu.addItem("Save", acceleratorMnemonicKey = KeyEvent.VK_S, saveSetList)

  frame.setJMenuBar( menuBar )

  position(title).at(4, 4).in(mainPanel)
  position(kickMonitor).below(title).withMargin(5).in(mainPanel)
  position(snareMonitor).toTheRightOf(kickMonitor).withMargin(5).in(mainPanel)
  position(handleMeter).toTheRightOf(snareMonitor).in(mainPanel)
  position(setlistUI).below(kickMonitor).withMargin(10).in(mainPanel)

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
  val currentSongUI = new SongUI(size, backgroundColor, save)
  val onNameChange = (name: String) => setListOpt.foreach {
    setList => {
      setList.setName(name)
      save()
    }
  }

  val nameField = new LabeledTextField("Set List", backgroundColor, 12, onNameChange)

  currentSongUI.setBackground(backgroundColor)
  position(nameField).atOrigin().in(this)
  position(currentSongUI).below(nameField).withMargin(4).in(this)

  def save(): Unit = {
    setListOpt.foreach { setlist => setlist.write() }
  }

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
  var currentPhraseOpt: Option[Phrase] = None

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
  val phraseEditor = new PhraseEditor(onInstrumentUpdated,
                                      onPhraseUpdated,
                                      backgroundColor)
  val phraseSelect = new PhraseList(onSelectPhrase,
                                     onEditPhraseSelected,
                                     onAddPhrase,
                                     backgroundColor)

  position(nameField).at(0, 4).in(this)
  position(channelField).toTheRightOf(nameField).withMargin(4).in(this)
  position(phraseSelect).below(nameField).withMargin(4).in(this)
  position(phraseEditor).toTheRightOf(phraseSelect).withMargin(4).in(this)

  def setSong(song: Song): Unit = {
    this.songOpt = Option(song)

    nameField.setText(song.name)
    channelField.setText(song.channel.toString)
    phraseEditor.setPhrase(song.phrases.head)
    songOpt.foreach { song =>
      phraseSelect.setPhrases(song.phrases)
    }
  }

  def onAddPhrase(): Unit = {
    songOpt.foreach { song =>
      val phrase = song.addPhrase()
      onSongUpdated()
      phraseEditor.setPhrase(phrase)
      phraseSelect.addPhrase(phrase)
      phraseEditor.requestEdit()
    }
  }

  def onSelectPhrase(phrase: Phrase): Unit = {
    phraseEditor.setPhrase(phrase)
  }

  def onEditPhraseSelected(phrase: Phrase): Unit = {
    phraseEditor.setPhrase(phrase)
    phraseEditor.requestEdit()
  }

  def onInstrumentUpdated(): Unit = {
    onSongUpdated()
  }

  def onPhraseUpdated(phrase: Phrase): Unit = {
    onSongUpdated()
    phraseSelect.phraseWasUpdated(phrase)
  }
}
