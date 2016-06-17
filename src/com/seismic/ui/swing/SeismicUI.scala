package com.seismic.ui.swing

import java.awt._
import java.awt.event.{MouseEvent, MouseListener}
import java.util.concurrent.Executors
import javax.swing.border.EtchedBorder
import javax.swing._

import com.daveclay.swing.color.GradientValueMap
import com.daveclay.swing.util.Position.position
import com.daveclay.swing.util.Size.setPreferredSize
import com.seismic.messages._
import com.seismic.ui.swing.SwingThreadHelper.invokeLater
import com.seismic.ui.swing.FontHelper._
import com.seismic.utils.ValueMapHelper
import com.seismic.utils.ValueMapHelper.map

class SeismicUIFactory {
  var seismicUIOpt: Option[SeismicUI] = None

  def build() = {
    val frame = new JFrame("Seismic")
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.pack()

    val seismicUI = new SeismicUI(frame, frame.getGraphics)
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

class SeismicUI(frame: JFrame, graphics: Graphics) {
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

  setPreferredSize(frame, 800, 300)
  mainPanel.setBackground(backgroundColor)

  title.setFont(titleFont)
  title.setForeground(new Color(200, 200, 210))

  position(title).at(4, 4).in(mainPanel)
  position(kickMonitor).below(title).withMargin(5).in(mainPanel)
  position(snareMonitor).toTheRightOf(kickMonitor).withMargin(5).in(mainPanel)
  position(handleMeter).toTheRightOf(snareMonitor).in(mainPanel)

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

class HandleMeter(font: Font,
                  size: Dimension,
                  graphics: Graphics) extends JLayeredPane {
  setPreferredSize(size)

  var value = 0

  val centerX = size.getWidth / 2f
  val centerY = size.getHeight / 2f

  val label = new JLabel("10234")
  label.setFont(font)
  label.setForeground(new Color(200, 200, 200))
  label.setOpaque(true)

  val indicator = new MeterCircleIndicator(size)

  position(indicator).at(0, 0).in(this)
  setLayer(indicator, 1)

  val labelDimensions = font.getFontMeasurement(graphics).getFontDimensions("1024")
  val labelX = centerX.toInt - labelDimensions.getWidth / 2f
  val labelY = centerY.toInt - labelDimensions.getHeight / 2f

  position(label).at(labelX.toInt, labelY.toInt).in(this)
  setLayer(label, 2)

  def setValue(value: Int): Unit = {
    this.value = value
    indicator.setAngle(map(value, 0, 1023, 0, (2f * Math.PI).toFloat))
    label.setText(f"$value%4s")
    repaint()
  }

  override def setBackground(color: Color): Unit = {
    indicator.setBackground(color)
    label.setBackground(color)
  }

  addMouseListener(new MouseListener() {
    override def mouseExited(e: MouseEvent): Unit = {}

    override def mouseClicked(e: MouseEvent): Unit = {}

    override def mouseEntered(e: MouseEvent): Unit = {}

    override def mousePressed(e: MouseEvent): Unit = {
      indicator.setAngle((Math.random() * (2 * Math.PI)).toFloat)
      repaint()
    }

    override def mouseReleased(e: MouseEvent): Unit = {}
  })
}


class MeterCircleIndicator(size: Dimension) extends JPanel {
  setPreferredSize(size)

  var angle = 0f
  val length = size.getWidth / 2f
  val centerX = size.getWidth / 2f
  val centerY = size.getHeight / 2f

  def setAngle(angle: Float): Unit = {
    this.angle = angle
  }

  override def paint(graphics: Graphics): Unit = {
    super.paint(graphics)

    val g2d = graphics.asInstanceOf[Graphics2D]

    val startX = centerX + Math.cos(angle) * (length - 20)
    val startY = centerY + Math.sin(angle) * (length - 20)

    val pointerX = startX + Math.cos(angle) * 20
    val pointerY = startY + Math.sin(angle) * 20

    val opposite = angle + Math.PI
    val endX = centerX + Math.cos(opposite) * length
    val endY = centerY + Math.sin(opposite) * length

    g2d.setColor(getBackground)
    g2d.fillRect(0, 0, getWidth, getHeight)

    g2d.setColor(Color.BLACK)
    g2d.fillOval(0, 0, getWidth, getHeight)

    g2d.setColor(new Color(90, 90, 90))
    g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL))
    g2d.drawLine(startX.toInt, startY.toInt, centerX.toInt, centerY.toInt)
    g2d.drawLine(centerX.toInt, centerY.toInt, endX.toInt, endY.toInt)
    g2d.setColor(new Color(255, 120, 0))
    g2d.drawLine(startX.toInt, startY.toInt, pointerX.toInt, pointerY.toInt)
  }
}


