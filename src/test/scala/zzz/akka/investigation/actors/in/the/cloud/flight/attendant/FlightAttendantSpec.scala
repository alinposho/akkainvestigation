package zzz.akka.investigation.actors.in.the.cloud.fight.attendant

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

@RunWith(classOf[JUnitRunner])
class FlightAttendantSpec extends TestKit(ActorSystem("FlightAttendantTestSystem", ConfigFactory.parseString("akka.scheduler.tick-duration = 1ms")))
  with WordSpec
  with ImplicitSender
  with MustMatchers
  with BeforeAndAfterAll {

  val Soda = "Soda"

  override def afterAll() = system.shutdown()

  "FlightAttendant" should {
    //    "get a drink when asked" in {
    //      val attendant = system.actorOf(TestFlightAttendant())
    //
    //      attendant ! GetDrink(Soda)
    //
    //      expectMsg(Drink(Soda))
    //    }

    "assist injured passengers before anything else" in {
      val attendant = TestActorRef[TestFlightAttendant]
      
      attendant ! Assist(testActor)

      expectMsg(Drink(MagicHealingPoltion))
    }
    
    "raise an exception when receiving an unknown msg" in {
      val attendant = TestActorRef[TestFlightAttendant]
            
      intercept[Exception] {
        attendant.receive('ThisIsSupposedToBeAnUnknowsMsg)//This will not swallow the exception raised for the unknown message
      }
    }
  }
  
}

object TestFlightAttendant {

  class TestFlightAttendant extends FlightAttendant with AttendantResponsiveness {
    val ONE_MILLI = 1
    override val maxResponseTimeMS = ONE_MILLI
  }

  def apply(): Props = Props[TestFlightAttendant]
}
