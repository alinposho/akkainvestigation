package zzz.akka.investigation.actors.in.the.cloud

import java.util.concurrent.CountDownLatch

object EventSourceSpy {
  val latch = new CountDownLatch(1)
}

trait EventSourceSpy extends EventSource {
  override def sendEvent[T](event: T): Unit = EventSourceSpy.latch.countDown()
  override def eventSourceReceive = {
    // Make sure we don't interfere with the other events by matching only the empty string event
    case "" => println("Received empty string");
  }
}