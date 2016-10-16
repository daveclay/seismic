package com.seismic.ui

import java.awt._
import java.awt.event._
import java.io.File
import javax.swing._

import com.daveclay.swing.util.Position.position
import com.seismic._
import com.seismic.io.Preferences
import com.seismic.ui.utils.SwingComponents.{backgroundColor, componentBGColor}
import com.seismic.ui.utils.layout.GridBagLayoutHelper
import com.seismic.ui.utils.{JSONFileChooser, SMenu, _}

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
  val triggerMonitorSize = new Dimension(contentSize.width - 12, 88)
  val triggerConfigSize = new Dimension(contentSize.width / 2 - 8, 40)
  //val setListSize = new Dimension(contentSize.width - 8, contentSize.height - 50 - titleSize - triggerMonitorSize.height - triggerConfigSize.height)
  val setListSize = new Dimension(contentSize.width - 200, contentSize.height - 200 - titleSize - triggerMonitorSize.height - triggerConfigSize.height)
  frame.setPreferredSize(contentSize)

  val mainPanel = frame.getContentPane
  val preferences = Preferences.getPreferences
  val triggerThresholds = preferences.triggerThresholds

  val title = SwingComponents.label("SEISMIC", SwingComponents.monoFont18)
  title.setForeground(SwingComponents.orangeColor)

  val onKickThresholdSet = (threshold: Int) => {
    triggerThresholds.kickThreshold = threshold
    preferences.save()
  }

  val onSnareThresholdSet = (threshold: Int) => {
    triggerThresholds.kickThreshold = threshold
    preferences.save()
  }

  val triggerMonitorUI = new TriggerMonitorUI(onKickThresholdSet, onSnareThresholdSet, triggerMonitorSize)
  triggerMonitorUI.setBackground(componentBGColor)

  val kickTriggerConfig = new ManualTriggerConfig("KICK", callbacks.triggerOn, callbacks.triggerOff, triggerConfigSize)
  kickTriggerConfig.setBackground(componentBGColor)

  val snareTriggerConfig = new ManualTriggerConfig("SNARE", callbacks.triggerOn, callbacks.triggerOff, triggerConfigSize)
  snareTriggerConfig.setBackground(componentBGColor)

  val fireKeyTrigger = (keyCode: Int) => {
    keyCode match {
      case KeyEvent.VK_S => snareTriggerConfig.fire()
      case KeyEvent.VK_K => kickTriggerConfig.fire()
    }
  }

  val releaseKeyTrigger = (keyCode: Int) => {
    keyCode match {
      case KeyEvent.VK_S => snareTriggerConfig.off()
      case KeyEvent.VK_K => kickTriggerConfig.off()
    }
  }

  val phraseNavigationKeyListener = new PhraseNavigationKeyListener(callbacks.prevPhrase,
                                                                     callbacks.nextPhrase,
                                                                     callbacks.patch,
                                                                     fireKeyTrigger,
                                                                     releaseKeyTrigger)

  val setlistUI = new SetlistUI(seismic,
                                 callbacks,
                                 setListSize,
                                 backgroundColor,
                                 componentBGColor)
  setlistUI.addKeyListener(phraseNavigationKeyListener)
  setlistUI.setOpaque(false)

  val onFileSelected = (file: File) => setlistUI.openSetList(seismic.openSetList(file))
  val fileChooser = new JSONFileChooser(frame, onFileSelected)

  val newSetList = () => setlistUI.newSetList()
  val saveSetList = () => setlistUI.save()
  val openSetList = () => fileChooser.show()

  val menuBar = new JMenuBar
  val fileMenu = new SMenu("File")
  menuBar.add(fileMenu)

  mainPanel.setBackground(backgroundColor)

  fileMenu.addItem("New Set List", acceleratorMnemonicKey = KeyEvent.VK_N, newSetList)
  fileMenu.addItem("Open", acceleratorMnemonicKey = KeyEvent.VK_O, openSetList)
  fileMenu.addItem("Save", acceleratorMnemonicKey = KeyEvent.VK_S, saveSetList)

  frame.setJMenuBar( menuBar )
  val contentPane = frame.getContentPane

  SwingComponents.addBorder(triggerMonitorUI)

  val helper = new GridBagLayoutHelper(contentPane)
  helper.position(title).withPadding(4).atOrigin().colspan(2).weightX(1).fillHorizontal().alignLeft().inParent()
  helper.position(triggerMonitorUI).withPadding(new Insets(0, 4, 0, 4)).colspan(2).below(title).fillHorizontal().alignLeft().inParent()
  helper.position(setlistUI).colspan(2).below(triggerMonitorUI).fill().weightY(1f).alignLeft().inParent()
  helper.position(kickTriggerConfig).withPadding(4).below(setlistUI).fillHorizontal().weightX(.5f).inParent()
  helper.position(snareTriggerConfig).withPadding(4).nextTo(kickTriggerConfig).fillHorizontal().weightX(.5f).inParent()
}



