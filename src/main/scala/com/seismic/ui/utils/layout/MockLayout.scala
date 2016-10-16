package com.seismic.ui.utils.layout

import java.awt._
import javax.swing.{JFrame, JLabel, JPanel}

import com.seismic.ui.TriggerMonitorUI
import com.seismic.ui.utils.SwingComponents

import scala.util.Random

object MockLayout {

  def main(args: Array[String]): Unit = {
    //Schedule a job for the event-dispatching thread:
    //creating and showing this application's GUI.
    javax.swing.SwingUtilities.invokeLater(new Runnable() {
      def run() {
        createAndShowGUI()
      }
    })
  }

  def createAndShowGUI() {
    val frame = new JFrame("GridBagLayoutDemo");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)

    //Set up the content pane.
    new MockLayout().addComponentsToPane(frame.getContentPane)

    //Display the window.
    frame.pack()
    frame.setVisible(true)
  }
}

class MockPanel(name: String) extends JPanel {
  setBackground(RandomColor.randomColor)
  this.add(new JLabel(name))
}

object RandomColor {
  val rand = new Random
  def randomColor = new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255))
}

class MockSetList extends JPanel {

  val songList = new MockPanel("Song List")
  val phraseList = new MockPanel("Phrase List")
  phraseList.setPreferredSize(new Dimension(140, 150))
  val songEditor = new MockPanel("Song Editor")

  val helper = new GridBagLayoutHelper(this)

  helper.position(songList).withPadding(4).atOrigin().fill().weightY(1).alignLeft().inParent()
  helper.position(phraseList).withPadding(4).nextTo(songList).fill().alignLeft().inParent()
  helper.position(songEditor).withPadding(4).nextTo(phraseList).fill().alignLeft().weightX(.8f).inParent()
}

class MockLayout {

  def addComponentsToPane(pane: Container) {
    pane.setLayout(new GridBagLayout())

    val title = SwingComponents.label("SEISMIC", SwingComponents.monoFont18)
    title.setForeground(SwingComponents.orangeColor)

    val triggerMonitor = new MockPanel("Trigger Monitor")
    val setList = new MockSetList
    val kickTrigger = new MockPanel("Kick Trigger")
    val snareTrigger = new MockPanel("Snare Trigger")

    val helper = new GridBagLayoutHelper(pane)
    helper.position(title).withPadding(4).atOrigin().colspan(2).weightX(1).fillHorizontal().alignLeft().inParent()
    helper.position(triggerMonitor).withPadding(4).colspan(2).below(title).fillHorizontal().alignLeft().inParent()
    helper.position(setList).withPadding(4).colspan(2).below(triggerMonitor).fill().weightY(1f).alignLeft().inParent()
    helper.position(kickTrigger).withPadding(4).below(setList).fillHorizontal().weightX(.5f).inParent()
    helper.position(snareTrigger).withPadding(4).nextTo(kickTrigger).fillHorizontal().weightX(.5f).inParent()
  }
}
