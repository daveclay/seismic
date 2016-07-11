package com.seismic.ui.swing

import java.awt._
import java.awt.event._
import java.io.File
import java.util
import javax.swing._

import com.daveclay.swing.color.ColorUtils
import com.daveclay.swing.util.Position.position
import com.daveclay.swing.util.Size.setPreferredSize
import com.seismic._
import com.seismic.messages._
import com.seismic.ui.swing.SwingThreadHelper.invokeLater
import com.seismic.ui.swing.draglist.{CellState, ListCallbacks, OrderableSelectionList}

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

  val newSetList = () => setlistUI.setSetList(seismic.newSetList)
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

class PhraseNavigationKeyListener(onUp: () => Unit,
                                  onDown: () => Unit) extends KeyAdapter {
  override def keyPressed(e: KeyEvent): Unit = {
    val code = e.getKeyCode
    if (code == KeyEvent.VK_UP || code == KeyEvent.VK_KP_UP) {
      onUp()
      e.consume()
    } else if (code == KeyEvent.VK_DOWN || code == KeyEvent.VK_KP_DOWN) {
      onDown()
      e.consume()
    }
  }
}

class SetlistUI(seismic: Seismic,
                size: Dimension,
                backgroundColor: Color,
                componentBGColor: Color) extends JPanel {

  setPreferredSize(size)

  val phraseNavigationKeyListener = new PhraseNavigationKeyListener(selectPreviousPhrase,
                                                                     selectNextPhrase)
  val nameField = new LabeledTextField("Set List", backgroundColor, 12, onSetListNameChange)

  val songCallbacks = ListCallbacks(acceptSong,
                                    songBackout,
                                    addSong,
                                    songsReordered)
  val songSelect = new OrderableSelectionList[Song](songCallbacks, Selector.renderSongItem)
  songSelect.setPreferredSize(new Dimension(250,400))
  songSelect.setBackground(componentBGColor)
  songSelect.addKeyListener(phraseNavigationKeyListener)

  val phraseCallbacks = new ListCallbacks(acceptPhrase,
                                          phraseBackout,
                                          addPhrase,
                                          phrasesReordered)
  val phraseSelect = new OrderableSelectionList[Phrase](phraseCallbacks, Selector.renderPhraseItem)
  phraseSelect.setPreferredSize(new Dimension(250,400))
  phraseSelect.setBackground(componentBGColor)
  phraseSelect.addKeyListener(phraseNavigationKeyListener)

  val selector = new Selector(songSelect, phraseSelect)
  selector.addKeyListener(phraseNavigationKeyListener)
  selector.setOpaque(false)

  val songEditor = new SongEditor(onSongUpdated, componentBGColor)
  val phraseEditor = new PhraseEditor(save, onPhraseUpdated, componentBGColor)

  val editor = new Editor(songEditor, phraseEditor)
  editor.setOpaque(false)

  position(nameField).atOrigin().in(this)
  position(selector).below(nameField).withMargin(4).in(this)
  position(editor).toTheRightOf(selector).withMargin(4).in(this)

  def save(): Unit = {
    seismic.save()
  }

  def onSetListNameChange(name: String): Unit = {
    seismic.setListOpt.foreach {
      setList => {
        setList.setName(name)
      }
    }
  }

  def selectPreviousSong(): Unit = {
    seismic.selectPreviousSong().foreach { song =>
      songWasSelected(song)
    }
  }

  def selectNextSong(): Unit = {
    seismic.selectNextSong().foreach { song =>
      songWasSelected(song)
    }
  }

  def selectPreviousPhrase(): Unit = {
    seismic.selectPreviousPhrase().foreach { phrase =>
      songWasSelected(phrase.song)
      phraseWasSelected(phrase)
    }
  }

  def selectNextPhrase(): Unit = {
    seismic.selectNextPhrase().foreach { phrase =>
      songWasSelected(phrase.song)
      phraseWasSelected(phrase)
    }
  }

  def acceptSong(song: Song): Unit = {
    songWasSelected(song)
  }

  def addSong(): Unit = {
    seismic.setListOpt.foreach { setList =>
      val song = setList.addSong()
      seismic.setCurrentSong(song)
      save()
      songSelect.addItem(song)
      phraseSelect.grabFocus()
    }
  }

  def songBackout(): Unit = {
    //TODO?
  }

  def onSongUpdated(song: Song): Unit = {
    save()
    songSelect.itemWasUpdated(song)
  }

  def songsReordered(songs: Seq[Song]): Unit = {
    seismic.setListOpt.foreach { setList =>
      setList.updateSongs(songs)
      seismic.save()
    }
  }

  def addPhrase(): Unit = {
    seismic.currentSongOpt.foreach { song =>
      val phrase = song.addPhrase()
      seismic.setCurrentPhrase(phrase)
      save()
      phraseSelect.addItem(phrase)
      phraseWasSelected(phrase)
      phraseEditor.grabFocus()
    }
  }

  def onPhraseUpdated(phrase: Phrase): Unit = {
    save()
    phraseSelect.itemWasUpdated(phrase)
  }

  def acceptPhrase(phrase: Phrase): Unit = {
    seismic.setCurrentPhrase(phrase)
    phraseWasSelected(phrase)
    phraseEditor.grabFocus()
  }

  def phraseBackout(): Unit = {
    songSelect.grabFocus()
  }

  def phrasesReordered(phrases: Seq[Phrase]): Unit = {
    println(s"phrases reordered: $phrases")
    seismic.currentSongOpt.foreach { song =>
      song.updatePhrases(phrases)
      seismic.save()
    }
  }

  private def setCurrentPhrase(phrase: Phrase): Unit = {
    seismic.setCurrentPhrase(phrase)
    phraseSelect.setCurrentSelectedItem(phrase)
    phraseEditor.setPhrase(phrase)
  }

  private def songWasSelected(song: Song): Unit = {
    println(s"song was selected ${song.name}")
    val phrases = song.phrases
    val phrase = phrases.head

    songSelect.setCurrentSelectedItem(song)
    songEditor.setSong(song)
    phraseSelect.setItems(phrases)
    phraseWasSelected(phrase)
  }

  private def phraseWasSelected(phrase: Phrase): Unit = {
    println(s"phrase was selected ${phrase.name} in ${phrase.song.name}")
    phraseEditor.setPhrase(phrase)
    phraseSelect.setCurrentSelectedItem(phrase)
  }

  def setSetList(setList: SetList): Unit = {
    nameField.setText(setList.name)
    songSelect.setItems(setList.songs)
    val song = setList.songs.head
    seismic.setCurrentSong(song)
    songWasSelected(song)
    setCurrentPhrase(song.phrases.head)
  }
}

class Editor(songEditor: SongEditor,
             phraseEditor: PhraseEditor) extends JPanel {
  setPreferredSize(new Dimension(Sizing.fitWidth(phraseEditor), 600))

  position(songEditor).atOrigin().in(this)
  position(phraseEditor).below(songEditor).withMargin(4).in(this)
}
