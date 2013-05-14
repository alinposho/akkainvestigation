package zzz.akka.investigation.actors.in.the.cloud

import java.util.concurrent.CountDownLatch
import akka.actor.ActorSystem
import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import org.scalatest.BeforeAndAfterAll
import akka.actor.Actor
import akka.testkit.TestActorRef
import akka.actor.Props
import java.util.concurrent.TimeUnit
import zzz.akka.investigation.actors.in.the.cloud.EventSource.RegisterListener
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

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

class SlicedAltimenter extends Altimeter with EventSourceSpy

@RunWith(classOf[JUnitRunner])
class AltimeterSpec extends TestKit(ActorSystem("AltemeterSpec"))
  with ImplicitSender
  with WordSpec
  with MustMatchers
  with BeforeAndAfterAll {

  import Altimeter._

  val MaxRateOfClimbChange = 1f
  val AboveMaxRateOfClimbChange = 2f

  override def afterAll(): Unit = system.shutdown()

  def actor() = {
    TestActorRef[SlicedAltimenter]
    //    TestActorRef[Altimeter](Props(new SlicedAltimenter())) // This way of 
    //instantiating Actors is deprecated
  }

  "Altimeter" should {

    "record rate of climb changes" in {
      val real = actor().underlyingActor
      real.receive(RateChange(MaxRateOfClimbChange))
      real.rateOfClimb must be(real.maxRateOfClimb)
    }

    "keep rate of climb changes within bounds" in {
      val real = actor().underlyingActor
      real.receive(RateChange(AboveMaxRateOfClimbChange))
      real.rateOfClimb must be(real.maxRateOfClimb)
    }

    "calculate rate changes" in {
      val realRef = system.actorOf(Altimeter())
      realRef ! RegisterListener(testActor)

      realRef ! RateChange(MaxRateOfClimbChange)

      fishForMessage() {
        case AltitudeUpdate(altitude) if (altitude == 0f) => false
        case AltitudeUpdate(altitude) => true
      }
    }

    "send events" in {
      val ref = actor()
      assertEventsAreSent()
    }

  }

  def assertEventsAreSent() {
    EventSourceSpy.latch.await(1, TimeUnit.SECONDS) must be(true)
  }
}

