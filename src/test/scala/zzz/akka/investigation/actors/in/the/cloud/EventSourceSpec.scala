package zzz.akka.investigation.actors.in.the.cloud

import akka.actor.{ Actor, ActorSystem, Props }
import akka.testkit.{ TestKit, TestActorRef, ImplicitSender }
import org.scalatest.{ WordSpec, BeforeAndAfterAll }
import org.scalatest.matchers.MustMatchers
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

class TestEventSource extends Actor with ProductionEventSource {
  def receive = eventSourceReceive
}

@RunWith(classOf[JUnitRunner])
class EventSourceSpec extends TestKit(ActorSystem("EventSourceSpec"))
  with WordSpec
  with MustMatchers
  with BeforeAndAfterAll {

  import EventSource._

  override def afterAll() { system.shutdown() }
  
  "EventSource" should {
    "allow us to register a listener" in {
      val real = TestActorRef[TestEventSource].underlyingActor
      real.receive(RegisterListener(testActor))
      real.listeners must contain(testActor)
    }
    "allow us to unregister a listener" in {
      val real = TestActorRef[TestEventSource].underlyingActor
      real.receive(RegisterListener(testActor))
      real.receive(UnregisterListener(testActor))
      real.listeners.size must be(0)
    }
    "send the event to our test actor" in {
      val testA = TestActorRef[TestEventSource]
      testA ! RegisterListener(testActor)
      testA.underlyingActor.sendEvent("Fibonacci")
      expectMsg("Fibonacci")
    }
  }
} 