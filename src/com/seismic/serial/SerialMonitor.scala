package com.seismic.serial

import java.util.concurrent.{ExecutorService, Executors, ThreadPoolExecutor}

import scala.concurrent.{ExecutionContext, Future}

trait SerialMessageHandler {
  def handleMessage(message: String)
}

class SerialMonitor(port: String) {

  implicit val executionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))
  val serialIO = new SerialIO(port)

  def start(handler: SerialMessageHandler): Unit = {
    serialIO.open(new SerialListener {
      override def dataAvailable(): Unit = {
        val message = serialIO.readStringUntil(10)
        if (message != null) {
          Future {
            // the handleMessage call is now on the thread from the Executor, not on Processing's animation thread.
            handler.handleMessage(message)
          }
        }
      }
    })
  }

}
