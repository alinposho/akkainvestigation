package zzz.akka.investigation.actors.in.the.cloud.flight.attendant

import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import com.typesafe.config.ConfigFactory
import FlightAttendant._
import TestFlightAttendant._
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.testkit.ImplicitSender
import akka.testkit.TestActorRef
import akka.testkit.TestKit
import org.scalatest.junit.JUnitRunner
import akka.actor.Cancellable
import akka.actor.ActorRef

@RunWith(classOf[JUnitRunner])
class FlightAttendantSpec extends TestKit(ActorSystem("FlightAttendantTestSystem", ConfigFactory.parseString("akka.scheduler.tick-duration = 1ms")))
  with WordSpec
  with ImplicitSender
  with MustMatchers
  with BeforeAndAfterAll {

  val Soda = "Soda"

  override def afterAll() = system.shutdown()

  "FlightAttendant" should {

    "assist injured passengers before anything else" in {
      val attendant = TestActorRef[TestFlightAttendant]

      attendant ! Assist(testActor)

      expectMsg(Drink(MagicHealingPoltion))
    }

    "be available to handle drink requests" in {
      val attendant = TestActorRef[TestFlightAttendant]

      attendant ! Busy_?

      expectMsg(No)
    }

    "get a drink when available to handle drink requests" in {
      val attendant = TestActorRef[TestFlightAttendant]

      assertAvailableToHandleDrinkRequests(attendant)
      attendant ! GetDrink(Soda)

      expectMsg(Drink(Soda))
    }
    
    def assertAvailableToHandleDrinkRequests(attendant: ActorRef) {
      attendant ! Busy_?
      expectMsg(No)
    }
  }
}

object TestFlightAttendant {

  class TestFlightAttendant extends FlightAttendant with AttendantResponsiveness {
    val ONE_MILLI = 1
    override val maxResponseTimeMS = ONE_MILLI
  }
}
