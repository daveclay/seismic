package com.seismic.ui.swing

import java.awt._
import java.awt.event._
import java.io.File
import java.util
import javax.swing._

import com.daveclay.swing.util.Position.position
import com.daveclay.swing.util.Size.setPreferredSize
import com.seismic._
import com.seismic.messages._
import com.seismic.ui.swing.SwingThreadHelper.invokeLater
import com.seismic.utils.ArrayUtils
import com.seismic.utils.ArrayUtils.wrapIndex

class SeismicUIFactory {
  var seismicUIOpt: Option[SeismicUI] = None

  def build(seismic: Seismic) = {
    val frame = new JFrame("Seismic")

    System.setProperty("apple.laf.useScreenMenuBar", "true")
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)

    val seismicUI = new SeismicUI(seismic, frame, frame.getGraphics)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
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

  val mainPanel = frame.getContentPane
  val backgroundColor = new Color(50, 50, 60)
  val componentBGColor = new Color(70, 70, 70)

  // TODO: build a factory that knows about all this shared styling
  // then ask the factory to create components, the factory sets the styling
  // remove all the styling from this layout and state handling.

  val titleFont = new Font("Arial", Font.PLAIN, 23)
  val monoFont = new Font("PT Mono", Font.PLAIN, 11)
  val title = SwingComponents.label("SEISMIC")

  mainPanel.setBackground(backgroundColor)

  val kickMonitor = new Meter("KICK", monoFont, new Dimension(300, 30))
  val snareMonitor = new Meter("SNARE", monoFont, new Dimension(300, 30))
  val triggerMonitors = Map(
    "KICK" -> kickMonitor,
    "SNARE" -> snareMonitor)

  val handleMeter = new HandleMeter(monoFont, new Dimension(80, 80))
  handleMeter.setBackground(backgroundColor)

  val setlistUI = new SetlistUI(seismic, new Dimension(1020, 600), backgroundColor, componentBGColor)

  setPreferredSize(frame, 1024, 800)
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
  position(handleMeter).toTheRightOf(snareMonitor).withMargin(4).in(mainPanel)
  position(setlistUI).below(kickMonitor).withMargin(60).in(mainPanel)

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
        monitor.setValue(value)
      case None =>
    }
    handleMeter.setValue(triggerOn.handleValue)
  }

  private def handleTriggerOff(triggerOff: TriggerOffMessage): Unit = {
    triggerMonitors.get(triggerOff.name) match {
      case Some(monitor) =>
        monitor.off()
      case None =>
    }
  }
}

class SetlistUI(seismic: Seismic,
                size: Dimension,
                backgroundColor: Color,
                componentBGColor: Color) extends JPanel {

  setPreferredSize(size)

  var setListOpt: Option[SetList] = None
  var currentSongOpt: Option[Song] = None
  val nameField = new LabeledTextField("Set List", backgroundColor, 12, onSetListNameChange)

  val songSelect = new SelectionList[Song](onShowSongSelected,
                                            onEditSongSelected,
                                            onAddSongSelected,
                                            onSongBackSelected,
                                            selectPreviousSong,
                                            selectNextSong,
                                            componentBGColor)

  val phraseSelect = new SelectionList[Phrase](onShowPhraseSelected,
                                                onEditPhraseSelected,
                                                onAddPhraseSelected,
                                                onPhraseBackSelected,
                                                selectPreviousSongFromPhrase,
                                                selectNextSongFromPhrase,
                                                componentBGColor)
  val songEditor = new SongEditor(onSongUpdated, componentBGColor)

  val phraseEditor = new PhraseEditor(save,
                                      onPhraseUpdated,
                                       componentBGColor)

  val selector = new Selector(songSelect, phraseSelect)
  selector.setOpaque(false)

  val editor = new Editor(songEditor, phraseEditor)
  editor.setOpaque(false)

  position(nameField).atOrigin().in(this)
  position(selector).below(nameField).withMargin(4).in(this)
  position(editor).toTheRightOf(selector).withMargin(4).in(this)

  def save(): Unit = {
    setListOpt.foreach { setlist => setlist.write() }
  }

  def onSetListNameChange(name: String): Unit = {
    setListOpt.foreach {
      setList => {
        setList.setName(name)
        save()
      }
    }
  }

  def onAddSongSelected(): Unit = {
    setListOpt.foreach { setList =>
      val song = setList.addSong()
      save()
      songSelect.addItem(song)
      onShowSongSelected(song)
      phraseSelect.requestFocusInWindow()
    }
  }

  def onShowSongSelected(song: Song): Unit = {
    if (!currentSongOpt.contains(song)) {
      setCurrentSong(song)
      setCurrentPhrase(song.phrases.head)
    }
  }

  def onEditSongSelected(song: Song): Unit = {
    phraseSelect.selectItem(song.phrases.head) // again, this will fire the "onShowPhraseSelected" callback
    phraseSelect.requestFocusInWindow()
  }

  def onSongBackSelected(song: Song): Unit = {
    //TODO?
  }

  def onSongUpdated(song: Song): Unit = {
    save()
    songSelect.itemWasUpdated(song)
  }

  def onAddPhraseSelected(): Unit = {
    currentSongOpt.foreach { song =>
      val phrase = song.addPhrase()
      save()
      phraseSelect.addItem(phrase)
      onEditPhraseSelected(phrase)
    }
  }

  def onShowPhraseSelected(phrase: Phrase): Unit = {
    setCurrentPhrase(phrase)
  }

  def onEditPhraseSelected(phrase: Phrase): Unit = {
    onShowPhraseSelected(phrase)
    phraseEditor.requestFocusInWindow()
  }

  def onPhraseUpdated(phrase: Phrase): Unit = {
    save()
    phraseSelect.itemWasUpdated(phrase)
  }

  def onPhraseBackSelected(phrase: Phrase): Unit = {
    phraseSelect.deselectAll() // TODO: leave the selected item in place? what do I show in the editor? keep the editor showing "currently playing" ?
    songSelect.requestFocusInWindow()
  }

  def selectPreviousSongFromPhrase(): Unit = {
    selectPreviousSong()
    setCurrentPhraseToLastPhraseInCurrentSong()
  }

  def selectNextSongFromPhrase(): Unit = {
    selectNextSong()
    setCurrentPhraseToFirstPhraseInCurrentSong()
  }

  def selectPreviousSongFromSongList(): Unit = {
    selectPreviousSong()
    setCurrentPhraseToFirstPhraseInCurrentSong()
  }

  def selectNextSongFromSongList(): Unit = {
    selectNextSong()
    setCurrentPhraseToFirstPhraseInCurrentSong()
  }

  private def selectPreviousSong(): Unit = {
    withSetListSong { (setList, song) =>
      val index = setList.songs.indexOf(song)
      selectSongAt(index - 1)
    }
  }

  private def selectNextSong(): Unit = {
    withSetListSong { (setList, song) =>
      val index = setList.songs.indexOf(song)
      selectSongAt(index + 1)
    }
  }

  private def selectSongAt(index: Int): Unit = {
    withSetListSong { (setList, song) =>
      val newSong = setList.songs(wrapIndex(index, setList.songs))
      setCurrentSong(newSong)
    }
  }

  private def setCurrentSong(song: Song): Unit = {
    currentSongOpt = Option(song)
    seismic.setCurrentSong(song)
    songSelect.indicateSelectedItem(song)
    phraseSelect.setItems(song.phrases)
    songEditor.setSong(song)
  }

  private def setCurrentPhraseToFirstPhraseInCurrentSong(): Unit = {
    currentSongOpt.foreach { song =>
      val phrase = song.phrases.head
      phraseSelect.selectItem(phrase)
    }
  }

  private def setCurrentPhraseToLastPhraseInCurrentSong(): Unit = {
    currentSongOpt.foreach { song =>
      val phrase = song.phrases.last
      phraseSelect.selectItem(phrase)
    }
  }

  private def setCurrentPhrase(phrase: Phrase): Unit = {
    seismic.setCurrentPhrase(phrase)
    phraseSelect.indicateSelectedItem(phrase)
    phraseEditor.setPhrase(phrase)
  }

  private def withSetListSong(f: (SetList, Song) => Unit): Unit = {
    setListOpt.foreach { setList =>
      currentSongOpt.foreach { song =>
        f(setList, song)
      }
    }
  }

  def setSetList(setList: SetList): Unit = {
    setListOpt = Option(setList)
    nameField.setText(setList.name)
    songSelect.setItems(setList.songs)
    val song = setList.songs.head
    songSelect.selectItem(song) // note this fires the callback!
    //onShowSongSelected(song)
    //onShowPhraseSelected(song.phrases.head)
  }
}

class Editor(songEditor: SongEditor,
             phraseEditor: PhraseEditor) extends JPanel {
  setPreferredSize(new Dimension(Sizing.fitWidth(phraseEditor), 600))

  position(songEditor).atOrigin().in(this)
  position(phraseEditor).below(songEditor).withMargin(4).in(this)
}

class SongEditor(onSongUpdated: (Song) => Unit,
                 backgroundColor: Color) extends JPanel {
  setPreferredSize(new Dimension(400, 30))
  setBackground(backgroundColor)
  SwingComponents.addBorder(this)

  var songOpt: Option[Song] = None
  var currentPhraseOpt: Option[Phrase] = None

  val onNameChange = (name: String) => songOpt.foreach { song =>
    song.setName(name)
    onSongUpdated(song)
  }

  val onChannelChange = (channel: String) => songOpt.foreach { song =>
    song.setChannel(channel.toInt)
    onSongUpdated(song)
  }

  val nameField = new LabeledTextField("Song", backgroundColor, 12, onNameChange)
  val channelField = new LabeledTextField("MIDI Channel", backgroundColor, 3, onChannelChange)

  position(nameField).at(4, 4).in(this)
  position(channelField).toTheRightOf(nameField).withMargin(4).in(this)

  def setSong(song: Song): Unit = {
    this.songOpt = Option(song)

    nameField.setText(song.name)
    channelField.setText(song.channel.toString)
  }
}

class Selector(songSelect: SelectionList[Song],
               phraseSelect: SelectionList[Phrase]) extends JPanel {

  setPreferredSize(new Dimension(Sizing.fitWidth(songSelect, phraseSelect) + 4, 600))

  position(songSelect).atOrigin().withMargin(4).in(this)
  position(phraseSelect).toTheRightOf(songSelect).withMargin(4).in(this)
}
