package zzz.akka.investigation.actors.in.the.cloud

import akka.actor.Props
import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.WordSpec
import akka.testkit.ImplicitSender
import org.scalatest.matchers.MustMatchers
import org.scalatest.BeforeAndAfterAll
import com.typesafe.config.ConfigFactory

object TestFlightAttendant {
  
  class TestFlightAttendant extends FlightAttendant with AttendantResponsiveness {
    val ONE_MILLI = 1
    override val maxResponseTimeMS = ONE_MILLI
  }
  
  def apply(): Props = Props[TestFlightAttendant]
}

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