package zzz.akka.investigation.actors.in.the.cloud.pilot

import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.testkit.TestKit
import akka.testkit.TestActorRef
import org.scalatest.BeforeAndAfterEach
import zzz.akka.investigation.actors.in.the.cloud.Plane
import org.scalatest.junit.JUnitRunner

// There has to be some better way of doing this
class DummyCopilot extends Actor {
  def receive = {
    case _ =>
  }
}

// There has to be some better way of doing this
class TestPilotParent extends Actor {

  def pilot = context.actorOf(Props[Pilot])

  def receive = {
    case msg =>
      throw new Exception(s"We received message $msg when we didn't expect it.")
  }
}

@RunWith(classOf[JUnitRunner])
class PilotSpec extends TestKit(ActorSystem("PilotSpecActorSystem"))
  with WordSpec
  with MustMatchers
  with BeforeAndAfterAll
  with BeforeAndAfterEach {

  import Pilot._
  import Plane._

  private var pilot: TestActorRef[Pilot] = null

  override def beforeEach() {
    pilot = TestActorRef[Pilot](Props(classOf[Pilot], system.deadLetters, 
        system.deadLetters, system.deadLetters, system.deadLetters))
  }

  override def afterAll = system.shutdown()

  "Pilot" should {

//    "ask its plane Actor for the controls when ReadyToGo" in {
//      pilot.underlyingActor.plane = testActor
//      pilot ! ReadyToGo
//      expectMsg(GiveMeControl)
//    }
//
//    "get a reference to its Copilot when ReadyToGo" in {
//      val copilot = createCopilot()
//      pilot ! ReadyToGo
//      assert(pilot.underlyingActor.copilot === copilot)
//    }

  }

  private def createCopilot() = {
    system.actorOf(Props[DummyCopilot], pilot.underlyingActor.copilotName)
  }

}