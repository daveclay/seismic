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
  def readStringUntil(byte: Int) = {
    val s = new StringBuilder
    s.append(pick("ON", "OFF"))
    s.append(",")
    s.append(pick("KICK", "SNARE"))
    s.append(",")
    s.append(random.nextInt(1023))
    s.append(",")
    s.append(random.nextInt(1023))
    s.toString
  }

  override def open(serialListener: SerialListener): Unit = {

    val scheduler = Executors.newSingleThreadScheduledExecutor
    val notifier = new Runnable() {
      override def run(): Unit = {
        serialListener.dataAvailable();
        scheduler.schedule(this, random.nextInt(1000), TimeUnit.MILLISECONDS)
      }
    }

    scheduler.schedule(notifier, random.nextInt(1000), TimeUnit.MILLISECONDS)
  }
}
