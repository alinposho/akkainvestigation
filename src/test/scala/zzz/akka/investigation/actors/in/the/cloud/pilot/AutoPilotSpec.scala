package zzz.akka.investigation.actors.in.the.cloud.pilot

import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import org.scalatest.matchers.MustMatchers
import org.scalatest.BeforeAndAfterAll
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import akka.actor.ActorSystem
import org.scalatest.junit.JUnitRunner
import com.typesafe.config.ConfigFactory
import akka.actor.Actor
import akka.actor.Props
import PilotsSpecConfig._
import Pilot._
import zzz.akka.investigation.actors.in.the.cloud.Plane.{ GiveMeControl, GetPerson, PersonReference }
import akka.actor.ActorRef
import scala.concurrent.duration._
import akka.testkit.TestActorRef

@RunWith(classOf[JUnitRunner])
class AutoPilotSpec extends TestKit(ActorSystem("AutoPilotSpec", ConfigFactory.parseString(PilotsSpecConfig.configStr)))
  with MustMatchers
  with ImplicitSender
  with WordSpec
  with BeforeAndAfterAll {

  override def afterAll() = system.shutdown()

  "Autopilot" should {
    "assign the testActor as its plane" in {
      val autopilot = TestActorRef[AutoPilot](Props(classOf[AutoPilot], testActor))

      assert(testActor === autopilot.underlyingActor.plane)
    }

    "take control of the plane when the copilot dies" in {
      // Prepare
      val copilot = system.actorOf(Props[FakeCoPilot], copilotName)
      val autopilot = TestActorRef[AutoPilot](Props(classOf[AutoPilot], testActor))
      autopilot ! ReadyToGo

      // Exercise
      expectMsg(GetPerson(copilotName))
      autopilot ! PersonReference(copilot)
      system.stop(copilot)

      // Verify      
      expectMsg(3 seconds, GiveMeControl)
    }
  }

}

class FakeCoPilot extends Actor {
  def receive = {
    case _ => throw new Exception("This exception is expected")
  }
}

