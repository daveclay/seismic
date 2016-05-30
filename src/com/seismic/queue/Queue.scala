package com.seismic.queue

import java.util.concurrent.{ArrayBlockingQueue, BlockingQueue, Executors, LinkedBlockingQueue}

import com.seismic.utils.RandomHelper.{pick, random}

import scala.concurrent.{ExecutionContext, Future}

class Producer(name: String, queue: BlockingQueue[Int]) extends Runnable {
  var counter = 1
  def run() {
    try {
      while (true) {
        val s = produce()
        queue.put(s)
      }
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
    }
  }

  def produce() = {
    // take time to produce something.
    val sleep = random.nextInt(1000)
    counter += 1
    Thread.sleep(sleep)
    println(f"Producer $name created $counter after $sleep ms");
    counter
  }
}


class Consumer(name: String, queue: BlockingQueue[Int]) extends Runnable {
  def run() {
    try {
      while (true) {
        consume(queue.take())
      }
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
    }
  }

  def consume(i: Int): Unit = {
    val sleep = random.nextInt(1000)
    Thread.sleep(sleep)
    println(f"Consumer $name ate $i after $sleep ms");
  }
}

object Queue {

  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(8))

  def main(args: Array[String]) {
    val q = new ArrayBlockingQueue[Int](100);
    val p = new Producer("Dr. Dre", q);
    val c1 = new Consumer("Alice", q);
    val c2 = new Consumer("Bob", q);
    new Thread(p).start();
    new Thread(c1).start();
    new Thread(c2).start();
  }
}

