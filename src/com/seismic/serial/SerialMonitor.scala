package com.seismic.serial

import java.util.concurrent.{ExecutorService, Executors, ThreadPoolExecutor, TimeUnit}

import scala.concurrent.{ExecutionContext, Future}
import com.seismic.utils.RandomHelper._

class SerialMonitor(handler: (String) => Any) {

  def start(port: String): Unit = {
    val serialIO = serialIOFor(port)

    implicit val executionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))

    serialIO.open(new SerialListener {
      override def dataAvailable(): Unit = {
        val message = serialIO.readStringUntil(10)
        if (message != null) {
          Future {
            // the handleMessage call is now on the thread from the Executor, not on Processing's animation thread.
            handler(message)
          }
        }
      }
    })
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
    val format = "ON,%s,%s,%s"

    def triggerOnMsg(name: String) = {
      val triggerValue = random.nextInt(1023).toString
      val handleValue = random.nextInt(1023).toString
      String.format(format, name, triggerValue, handleValue)
    }

    def triggerOffMsg(name: String) = {
      f"OFF,$name"
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

  val kick = Trigger("KICK")
  val snare = Trigger("SNARE")
  val kickOn = () => kick.triggerOn()
  val snareOn = () => snare.triggerOn()
  val kickOff = () => kick.triggerOff()
  val snareOff = () => snare.triggerOff()

  def readStringUntil(byte: Int) = {
    kick.on match {
      case true => kickOff()
      case false =>
        snare.on match {
          case true => snareOff()
          case false => pick(kickOn, snareOn)()
        }
    }
  }

  override def open(serialListener: SerialListener): Unit = {

    val scheduler = Executors.newSingleThreadScheduledExecutor
    val notifier = new Runnable() {
      override def run(): Unit = {
        serialListener.dataAvailable()
        scheduler.schedule(this, random.nextInt(1000) + 400, TimeUnit.MILLISECONDS)
      }
    }

    scheduler.schedule(notifier, random.nextInt(1000) + 400, TimeUnit.MILLISECONDS)
  }
}
