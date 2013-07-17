package zzz.akka.investigation.actors.in.the.cloud.heading

import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import akka.testkit.TestKit
import akka.actor.ActorSystem
import akka.testkit.ImplicitSender
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import org.scalatest.BeforeAndAfterAll
import zzz.akka.investigation.actors.in.the.cloud.EventSourceSpy
import java.util.concurrent.TimeUnit
import akka.testkit.TestActorRef
import zzz.akka.investigation.actors.in.the.cloud.EventSource
import akka.actor.Props
import zzz.akka.investigation.actors.in.the.cloud.EventSourceSpySpec

class SlicedHeadingIndicator extends HeadingIndicator with EventSourceSpy

@RunWith(classOf[JUnitRunner])
class HeadingIndicatorSpec extends TestKit(ActorSystem("HeadingIndicator"))
							  with EventSourceSpySpec
							  with ImplicitSender
							  with WordSpec
							  with MustMatchers
							  with BeforeAndAfterAll {

  import EventSource.RegisterListener
  import HeadingIndicator._

  val MaxRateOfBankChange = 1f
  val AboveMaxRateOfBankChange = 2f
  val BellowMinRateOfBankChange = -2f

  type T = SlicedHeadingIndicator
  override def createTestActor() = TestActorRef[SlicedHeadingIndicator]
  override def afterAll(): Unit = system.shutdown()

  "HeadingIndicator" should {

    "record rate of climb changes" in {
      val actorRef = createTestActor().underlyingActor
      actorRef.receive(BankChange(MaxRateOfBankChange))
      actorRef.rateOfBank must be(MaxRateOfBank)
    }

    "keep rate of bank changes within the upper bounds" in {
      val actorRef = createTestActor().underlyingActor
      actorRef.receive(BankChange(AboveMaxRateOfBankChange))
      actorRef.rateOfBank must be(MaxRateOfBank)
    }

    "keep rate of bank changes within the lower bounds" in {
      val actorRef = createTestActor().underlyingActor
      actorRef.receive(BankChange(BellowMinRateOfBankChange))
      actorRef.rateOfBank must be(MinRateOfBank)
    }

    "calculate rate changes and send them to the test actor" in {
      val realActor = system.actorOf(HeadingIndicator())
      realActor ! RegisterListener(testActor)

      realActor ! BankChange(MaxRateOfBankChange)

      fishForMessage() {
        case HeadingUpdate(heading) if (heading == 0f) => false
        case HeadingUpdate(heading) => true
      }
    }
  }
}