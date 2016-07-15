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

case class SeismicSerialCallbacks(prevPhrase: () => Unit,
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

  val mainPanel = frame.getContentPane
  val backgroundColor = new Color(50, 50, 60)
  val componentBGColor = new Color(70, 70, 70)

  // TODO: build a factory that knows about all this shared styling
  // then ask the factory to create components, the factory sets the styling
  // remove all the styling from this layout and state handling.

  val titleFont = new Font("Arial", Font.PLAIN, 23)
  val monoFont = new Font("PT Mono", Font.PLAIN, 11)
  val title = SwingComponents.label("SEISMIC")
  val triggerMonitorUI = new TriggerMonitorUI(monoFont)
  triggerMonitorUI.setBackground(componentBGColor)

  val setlistUI = new SetlistUI(seismic,
                                 callbacks,
                                 new Dimension(1020, 600),
                                 backgroundColor,
                                 componentBGColor)

  val onFileSelected = (file: File) => setlistUI.openSetList(seismic.openSetList(file))
  val fileChooser = new JSONFileChooser(frame, onFileSelected)

  val newSetList = () => setlistUI.newSetList()
  val saveSetList = () => setlistUI.save()
  val openSetList = () => fileChooser.show()

  val menuBar = new JMenuBar
  val fileMenu = new SMenu("File")
  menuBar.add(fileMenu)

  setPreferredSize(frame, 1024, 800)
  setlistUI.setBackground(backgroundColor)
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
  position(setlistUI).below(triggerMonitorUI).withMargin(4).in(mainPanel)
}


