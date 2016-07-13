package com.seismic.serial

import java.util.concurrent._

import com.seismic.messages.Message

import scala.concurrent.{ExecutionContext, Future}
import com.seismic.utils.RandomHelper._

class SerialMonitor() {

  implicit val executionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
  var handlerOpt: Option[(String => Unit)] = None

  def setHandler(handler: (String) => Unit): Unit = {
    handlerOpt = Option(handler)
  }

  def start(port: String): Unit = {
    val serialIO = serialIOFor(port)

    serialIO.open(() => {
      val message = serialIO.readStringUntil(10)
      if (message != null) {
        handleMessage(message)
      }
    })
  }

  private def handleMessage(message: String): Unit = {
    handlerOpt.foreach { handler =>
      Future {
        // handle on some other thread; though at this point that's the midi thread too.
        try {
          handler(message)
        } catch {
          case e: Exception => e.printStackTrace()
        }
      }
    }
  }

  private def serialIOFor(port: String) = {
    if (port.equals("mock")) {
      new MockSerialIO
    } else {
      new StandardSerialIO(port)
    }
  }
}

class MockSerialIO extends SerialIO {

  object Trigger {
    val format = "T,ON,%s,%s,%s"

    def triggerOnMsg(name: String) = {
      val triggerValue = random.nextInt(1023).toString
      val handleValue = random.nextInt(1023).toString
      String.format(format, name, triggerValue, handleValue)
    }

    def triggerOffMsg(name: String) = {
      f"T,OFF,$name"
    }
  }

  case class Trigger(name: String) {
    import Trigger._
    var on = false

    def triggerOn() = {
      on = true
      triggerOnMsg(name)
    }

    def triggerOff() = {
      on = false
      triggerOffMsg(name)
    }
  }

  var io: ActiveMockSerialIO = null

  /**
    * called by another thread after being notified to read serial data.
 *
    * @param byte
    * @return
    */
  def readStringUntil(byte: Int) = {
    io.readStringUntil(byte)
  }

  def trigger(message: Message): Unit = {
  }

  override def open(serialListener: SerialListener): Unit = {
    io = new ActiveMockSerialIO(serialListener)
    io.scheduleSerialEvent()
  }

  class ActiveMockSerialIO(serialListener: SerialListener) {

    val kick = Trigger("KICK")
    val snare = Trigger("SNARE")
    val kickOn = () => kick.triggerOn()
    val snareOn = () => snare.triggerOn()
    val kickOff = () => kick.triggerOff()
    val snareOff = () => snare.triggerOff()

    val drumTriggerScheduledExecutor = Executors.newSingleThreadScheduledExecutor
    val notifierPool = Executors.newFixedThreadPool(1)

    var queue = new ArrayBlockingQueue[String](100)

    // Hi, I am a SerialIO, and this is where I tell the world that a serial message is available by calling
    // dataAvailable.
    val scheduledTriggerMessageBuilder = new Runnable() {
      override def run(): Unit = {
        val message = createNextTriggerMessage()
        queue.put(message)

        serialListener.dataAvailable() // triggers listeners to call `readStringUtil`
        scheduleSerialEvent()
      }
    }

    /**
      * called by another thread after being notified to read serial data.
 *
      * @param byte
      * @return
      */
    def readStringUntil(byte: Int) = {
      queue.poll()
    }

    def trigger(message: String): Unit = {
      queue.put(message)
      notifierPool.execute(() => serialListener.dataAvailable() )
    }

    private def createNextTriggerMessage() = {
      kick.on match {
        case true => kickOff()
        case false =>
          snare.on match {
            case true => snareOff()
            case false => pick(kickOn, snareOn)()
          }
      }
    }

    def scheduleSerialEvent(): Unit = {
      drumTriggerScheduledExecutor.schedule(scheduledTriggerMessageBuilder,
                                             random.nextInt(1000) + 400,
                                             TimeUnit.MILLISECONDS)
    }
  }
}
