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
import com.sun.javafx.scene.control.skin.LabeledText

case class SeismicSerialCallbacks(triggerOn: (String, Int, Int) => Unit,
                                  triggerOff: (String) => Unit,
                                  prevPhrase: () => Unit,
                                  nextPhrase: () => Unit,
                                  patch: (Int) => Unit)

class SeismicUIFactory {
  var seismicUIOpt: Option[SeismicUI] = None

  def build(seismic: Seismic, callbacks: SeismicSerialCallbacks) = {
    val frame = new JFrame("Seismic")

    System.setProperty("apple.laf.useScreenMenuBar", "true")
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)

    val seismicUI = new SeismicUI(seismic, callbacks, frame)
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.pack()
    frame.setVisible(true)

    seismicUIOpt = Some(seismicUI)

    seismicUI
  }
}

class SeismicUI(seismic: Seismic,
                callbacks: SeismicSerialCallbacks,
                frame: JFrame) {

  val titleSize = 23
  val contentSize = new Dimension(1024, 700)
  val triggerSize = new Dimension(contentSize.width - 12, 88)
  val triggerConfigSize = new Dimension(contentSize.width / 2 - 8, 40)
  val setListSize = new Dimension(contentSize.width - 8, contentSize.height - 50 - titleSize - triggerSize.height - triggerConfigSize.height)
  frame.setPreferredSize(contentSize)

  val mainPanel = frame.getContentPane
  val backgroundColor = new Color(30, 30, 43)
  val componentBGColor = new Color(50, 50, 50)

  // TODO: build a factory that knows about all this shared styling
  // then ask the factory to create components, the factory sets the styling
  // remove all the styling from this layout and state handling.

  val titleFont = new Font("Arial", Font.PLAIN, titleSize)
  val monoFont = new Font("PT Mono", Font.PLAIN, 11)
  val title = SwingComponents.label("SEISMIC")
  val triggerMonitorUI = new TriggerMonitorUI(monoFont, triggerSize)
  triggerMonitorUI.setBackground(componentBGColor)

  // TODO: settings textbox that sets the value of a "t" keypress
  val phraseNavigationKeyListener = new PhraseNavigationKeyListener(callbacks.prevPhrase,
                                                                     callbacks.nextPhrase,
                                                                     callbacks.patch,
                                                                     () => {
                                                                       println(s"pressed some letter, I think")
                                                                     })

  val setlistUI = new SetlistUI(seismic,
                                 callbacks,
                                 setListSize,
                                 backgroundColor,
                                 componentBGColor)
  setlistUI.addKeyListener(phraseNavigationKeyListener)
  setlistUI.setOpaque(false)

  val kickTriggerConfig = new ManualTriggerConfig("Kick", triggerConfigSize)
  kickTriggerConfig.setBackground(componentBGColor)

  val snareTriggerConfig = new ManualTriggerConfig("Snare", triggerConfigSize)
  snareTriggerConfig.setBackground(componentBGColor)

  val onFileSelected = (file: File) => setlistUI.openSetList(seismic.openSetList(file))
  val fileChooser = new JSONFileChooser(frame, onFileSelected)

  val newSetList = () => setlistUI.newSetList()
  val saveSetList = () => setlistUI.save()
  val openSetList = () => fileChooser.show()

  val menuBar = new JMenuBar
  val fileMenu = new SMenu("File")
  menuBar.add(fileMenu)

  mainPanel.setBackground(backgroundColor)

  title.setFont(titleFont)
  title.setForeground(new Color(200, 200, 210))

  fileMenu.addItem("New Set List", acceleratorMnemonicKey = KeyEvent.VK_N, newSetList)
  fileMenu.addItem("Open", acceleratorMnemonicKey = KeyEvent.VK_O, openSetList)
  fileMenu.addItem("Save", acceleratorMnemonicKey = KeyEvent.VK_S, saveSetList)

  frame.setJMenuBar( menuBar )

  SwingComponents.addBorder(triggerMonitorUI)

  position(title).at(4, 4).in(mainPanel)
  position(triggerMonitorUI).below(title).in(mainPanel)
  position(setlistUI).below(triggerMonitorUI).in(mainPanel)
  position(kickTriggerConfig).below(setlistUI).withMargin(4).in(mainPanel)
  position(snareTriggerConfig).toTheRightOf(kickTriggerConfig).withMargin(4).in(mainPanel)
}

class ManualTriggerConfig(triggerName: String, size: Dimension) extends JPanel {
  SwingComponents.addBorder(this)
  setPreferredSize(size)

  val label = new JLabel(triggerName)
  val triggerValueField = new LabeledTextField("Trigger Value", 5, onChange)
  val handleValueField = new LabeledTextField("Handle Value", 5, onChange)

  position(label).at(4, 4).in(this)
  position(triggerValueField).toTheRightOf(label).withMargin(4).in(this)
  position(handleValueField).toTheRightOf(triggerValueField).withMargin(4).in(this)

  def onChange(value: String): Unit = {
    println(s"ok $value")
  }
}

