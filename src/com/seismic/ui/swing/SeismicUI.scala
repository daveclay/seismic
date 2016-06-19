package com.seismic.ui.swing

import java.awt._
import java.util.concurrent.Executors
import javax.swing._

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

  val setlistUI = new SetlistUI(new Dimension(800, 500),
                                 backgroundColor)

  setlistUI.setSetList(seismic.setList)
  setlistUI.setBackground(backgroundColor)

  setPreferredSize(frame, 800, 600)
  mainPanel.setBackground(backgroundColor)

  title.setFont(titleFont)
  title.setForeground(new Color(200, 200, 210))

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

  val currentSongUI = new SongUI(size, backgroundColor)
  currentSongUI.setBackground(backgroundColor)
  position(currentSongUI).atOrigin().in(this)

  def setSetList(setList: SetList): Unit = {
    currentSongUI.setSong(setList.songs(0))
  }
}

class SongUI(size: Dimension,
             backgroundColor: Color) extends JPanel {
  setPreferredSize(size)
  setBackground(backgroundColor)

  var songOpt: Option[Song] = None

  val nameField = new LabeledTextField("Name", backgroundColor, 12)
  val channelField = new LabeledTextField("MIDI Channel", backgroundColor, 3)
  val phraseUI = new PhraseUI(new Dimension(size.width, size.height), backgroundColor)

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

class PhraseUI(size: Dimension, backgroundColor: Color) extends JPanel {
  setPreferredSize(size)
  setBackground(backgroundColor)

  var curentPhraseOpt: Option[Phrase] = None
  var instrumentUIOpts: Option[Array[InstrumentUI]] = None
  val kickLabel = SwingComponents.textField(backgroundColor, 7)
  kickLabel.setText("Kick")
  val snareLabel = SwingComponents.textField(backgroundColor, 7)
  snareLabel.setText("Snare")

  val nameField = new LabeledTextField("Name", backgroundColor, 12)
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

class InstrumentUI(instrument: Instrument, size: Dimension, backgroundColor: Color) extends JPanel {
  setPreferredSize(size)
  setBackground(backgroundColor)
  val nameField = new LabeledTextField("Note", backgroundColor, 3)
  // nameField.setText(instrument.notes) // hah, I'm tired, I forgot how this works.
  position(nameField).atOrigin().in(this)
}

class LabeledTextField(labelText: String, backgroundColor: Color, size: Int) extends JPanel {

  val label = new JLabel(labelText)
  label.setBackground(backgroundColor)
  label.setForeground(new Color(200, 200, 200))

  setBackground(backgroundColor)

  val textField = SwingComponents.textField(Color.BLACK, size)

  val labelSize = label.getPreferredSize
  var textFieldSize = textField.getPreferredSize

  setPreferredSize(new Dimension(labelSize.width + textFieldSize.width, textFieldSize.height))

  position(label).atOrigin().in(this)
  position(textField).toTheRightOf(label).withMargin(10).in(this)

  def setText(text: String): Unit = {
    textField.setText(text)
  }
}

object SwingComponents {

  def textField(backgroundColor: Color, size: Int) = {
    val field = new JTextField(size)
    field.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2))
    field.setForeground(new Color(200, 200, 200))
    field.setBackground(backgroundColor)
    field.setOpaque(true)
    field.setEditable(false)
    field
  }
}









