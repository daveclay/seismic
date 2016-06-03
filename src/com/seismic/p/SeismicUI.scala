package com.seismic.p

import java.awt.{Color, Container, Font}
import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue}
import javax.swing.{JFrame, JLabel, JPanel, SwingUtilities}

import com.daveclay.swing.color.ColorUtils
import com.daveclay.swing.util.Position.position
import com.daveclay.swing.util.Size.fit
import com.daveclay.swing.util.{Position, Size}
import com.seismic.messages._
import com.seismic.swing.SwingThreadHelper.invokeLater

class SeismicUI {
  val frame = new JFrame("Seismic")
  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
  val mainPanel = frame.getContentPane
  val title = new JLabel("Seismic")

  def start(): Unit = {
    layout()
    frame.pack()
    frame.setVisible(true)
  }

  def handleMessage(message: Message): Unit = {
    invokeLater { () =>
      System.out.println(Thread.currentThread().getName + " with message " + message)
    }
  }

  private def layout(): Unit = {
    Size.setPreferredSize(frame, 600, 300)
    mainPanel.setBackground(new Color(50, 50, 60))

    title.setFont(new Font("Arial", Font.PLAIN, 18))
    title.setForeground(new Color(200, 200, 210))

    position(title).at(4, 4).in(mainPanel)

  }
}



