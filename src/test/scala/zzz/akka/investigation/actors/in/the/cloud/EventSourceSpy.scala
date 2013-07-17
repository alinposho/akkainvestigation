package zzz.akka.investigation.actors.in.the.cloud

import java.util.concurrent.CountDownLatch
import org.scalatest.matchers.MustMatchers
import org.scalatest.WordSpec
import akka.testkit.TestActorRef
import java.util.concurrent.TimeUnit
import akka.actor.Actor

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

trait EventSourceSpySpec extends MustMatchers with WordSpec {
  def createTestActor[T <: Actor](): TestActorRef[T]

  "EventSource" should {
    "send events" in {
      val ref = createTestActor()
      assertEventsAreSent()
    }
  }

  def assertEventsAreSent() {
    EventSourceSpy.latch.await(1, TimeUnit.SECONDS) must be(true)
  }
}