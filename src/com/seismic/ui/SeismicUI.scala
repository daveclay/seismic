package com.seismic.ui

import java.awt._
import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue}
import javax.swing.{JFrame, JLabel, JPanel, SwingUtilities}

import com.daveclay.swing.color.ColorUtils
import com.daveclay.swing.util.Position.position
import com.daveclay.swing.util.Size.{fit, setPreferredSize}
import com.daveclay.swing.util.{Position, Size}
import com.seismic.messages._
import com.seismic.ui.swing.FontMeasurements
import com.seismic.ui.swing.SwingThreadHelper.invokeLater

class SeismicUI {

  val frame = new JFrame("Seismic")
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  val mainPanel = frame.getContentPane
  val titleFont = new Font("Arial", Font.PLAIN, 18)
  val title = new JLabel("Seismic")
  val kickMonitor = new JLabel

  def start(): Unit = {
    frame.pack()
    layout()
    frame.pack()
    frame.setVisible(true)
  }

  def handleMessage(message: Message): Unit = {
    invokeLater { () =>
      System.out.println(Thread.currentThread().getName + " with message " + message)
      message match {
        case triggerOn: TriggerOnMessage => handleTriggerOn(triggerOn)
        case triggerOff: TriggerOffMessage => handleTriggerOff(triggerOff)
        case _ => System.out.println(f"Unknown message: $message")
      }
    }
  }

  private def handleTriggerOn(triggerOn: TriggerOnMessage): Unit = {
    kickMonitor.setText(triggerOn.triggerValue.toString)
    kickMonitor.repaint()
  }

  private def handleTriggerOff(triggerOff: TriggerOffMessage): Unit = {
    kickMonitor.setText("")
    kickMonitor.repaint()
  }

  private def layout(): Unit = {
    val titleFontMeasurements = new FontMeasurements(titleFont, frame.getGraphics)

    setPreferredSize(frame, 600, 300)
    mainPanel.setBackground(new Color(50, 50, 60))

    title.setFont(titleFont)
    title.setForeground(new Color(200, 200, 210))

    kickMonitor.setFont(titleFont)
    kickMonitor.setForeground(new Color(200, 200, 210))
    setPreferredSize(kickMonitor, 100, titleFontMeasurements.getFontHeight() + 2)

    position(title).at(4, 4).in(mainPanel)
    position(kickMonitor).below(title).withMargin(5).in(mainPanel)

  }
}





