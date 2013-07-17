package zzz.akka.investigation.actors.in.the.cloud.altimeter

import java.util.concurrent.TimeUnit
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import akka.actor.ActorSystem
import akka.actor.actorRef2Scala
import akka.testkit.ImplicitSender
import akka.testkit.TestActorRef
import akka.testkit.TestKit
import zzz.akka.investigation.actors.in.the.cloud.EventSource.RegisterListener
import zzz.akka.investigation.actors.in.the.cloud.EventSourceSpy
import zzz.akka.investigation.actors.in.the.cloud.altimeter.Altimeter.AltitudeUpdate
import zzz.akka.investigation.actors.in.the.cloud.altimeter.Altimeter.RateChange
import org.scalatest.junit.JUnitRunner
import zzz.akka.investigation.actors.in.the.cloud.EventSourceSpySpec

class SlicedAltimenter extends Altimeter with EventSourceSpy

@RunWith(classOf[JUnitRunner])
class AltimeterSpec extends TestKit(ActorSystem("AltemeterSpec"))
					  with EventSourceSpySpec
					  with ImplicitSender
					  with WordSpec
					  with MustMatchers
					  with BeforeAndAfterAll {

  import Altimeter._

  val MaxRateOfClimbChange = 1f
  val AboveMaxRateOfClimbChange = 2f

  type T = SlicedAltimenter
  override def createTestActor() = TestActorRef[SlicedAltimenter]
  override def afterAll(): Unit = system.shutdown()

  "Altimeter" should {
    "record rate of climb changes" in {
      val realActor = createTestActor().underlyingActor
      realActor.receive(RateChange(MaxRateOfClimbChange))
      realActor.rateOfClimb must be(realActor.maxRateOfClimb)
    }

    "keep rate of climb changes within bounds" in {
      val real = createTestActor().underlyingActor
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
  }
}

