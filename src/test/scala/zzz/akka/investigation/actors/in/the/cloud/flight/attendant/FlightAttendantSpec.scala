package zzz.akka.investigation.actors.in.the.cloud.fight.attendant

import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import zzz.akka.investigation.actors.in.the.cloud.fight.attendant.FlightAttendant.Drink
import zzz.akka.investigation.actors.in.the.cloud.fight.attendant.FlightAttendant.GetDrink
import org.scalatest.junit.JUnitRunner

object TestFlightAttendant {

  class TestFlightAttendant extends FlightAttendant with AttendantResponsiveness {
    val ONE_MILLI = 1
    override val maxResponseTimeMS = ONE_MILLI
  }

  def apply(): Props = Props[TestFlightAttendant]
}

@RunWith(classOf[JUnitRunner])
class FlightAttendantSpec extends TestKit(ActorSystem("FlightAttendantTestSystem",
  ConfigFactory.parseString("akka.scheduler.tick-duration = 1ms")))
  with WordSpec
  with ImplicitSender
  with MustMatchers
  with BeforeAndAfterAll {

  import FlightAttendant._

  val Soda = "Soda"

  override def afterAll() = system.shutdown()

  "FlightAttendant" should {
    "get a drink when asked" in {
      val attendant = system.actorOf(TestFlightAttendant())

      attendant ! GetDrink(Soda)

      expectMsg(Drink(Soda))
    }
  }

}