package zzz.akka.investigation.actors.in.the.cloud

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import akka.testkit.ImplicitSender
import org.scalatest.BeforeAndAfterAll
import akka.testkit.TestProbe
import akka.testkit.TestActorRef
import akka.actor.Props
import akka.actor.ActorRef
import zzz.akka.investigation.actors.in.the.cloud.altimeter.Altimeter
import akka.actor.Actor
import zzz.akka.investigation.actors.in.the.cloud.heading.HeadingIndicator

@RunWith(classOf[JUnitRunner])
class ControlSurfacesSpec extends TestKit(ActorSystem("ControlSurfacesSpec"))
							  with WordSpec
							  with MustMatchers
							  with ImplicitSender
							  with BeforeAndAfterAll {
  import ControlSurfaces._
  import Altimeter._
  import HeadingIndicator._

  val plane = TestActorRef[DummyActor]("plane")
  val newController = TestActorRef[DummyActor]("newController")
  val controller = TestActorRef[DummyActor]("controller")

  override def afterAll() = system.shutdown()
  def createDummyActor = TestActorRef[DummyActor]

  "ControlSurfaces" should {

    "send commads to the altimeter when comming from the controller" in {
      val altimeter = testActor
      val controlSurfaces = createControlSurfacesForAltimeter(altimeter)

      val amount = 768.777f
      controlSurfaces.!(StickForward(amount))(controller)
      expectMsg(RateChange(-amount))

      controlSurfaces.!(StickBack(amount))(controller)
      expectMsg(RateChange(amount))
    }

    def createControlSurfacesForAltimeter(altimeter: ActorRef) = {
      val controlSurfaces = TestActorRef[ControlSurfaces](Props(classOf[ControlSurfaces], plane, altimeter, createDummyActor))
      controlSurfaces.!(HasControl(controller))(plane)

      controlSurfaces
    }

    "send commads to the heading indicator when comming from the controller" in {
      val heading = testActor
      val controlSurfaces = createControlSurfacesForHeading(heading)

      val amount = 768.777f
      controlSurfaces.!(StickLeft(amount))(controller)
      expectMsg(BankChange(-amount))

      controlSurfaces.!(StickRight(amount))(controller)
      expectMsg(BankChange(amount))
    }

    def createControlSurfacesForHeading(heading: ActorRef) = {
      val controlSurfaces = TestActorRef[ControlSurfaces](Props(classOf[ControlSurfaces], plane, createDummyActor, heading))
      controlSurfaces.!(HasControl(controller))(plane)

      controlSurfaces
    }

    "change the controller when the plane asks" in {
      val controlSurfaces = TestActorRef[ControlSurfaces](Props(classOf[ControlSurfaces], plane, testActor, testActor))

      controlSurfaces.!(HasControl(newController))(plane)

      assertControllerWasChangedTo(newController, controlSurfaces)
    }

    def assertControllerWasChangedTo(newController: ActorRef, controlSurfaces: ActorRef) {
      val amount = 89898.9f
      controlSurfaces.!(StickBack(amount))(newController)

      expectMsg(RateChange(amount))
    }

    "NOT change the controller when anyone except for the plane asks it to" in {
      val controlSurfaces = TestActorRef[ControlSurfaces](Props(classOf[ControlSurfaces], plane, testActor, testActor))

      controlSurfaces ! HasControl(newController)

      assertControllerWasNotChangedTo(newController, controlSurfaces)
    }

    def assertControllerWasNotChangedTo(newController: ActorRef, controlSurfaces: ActorRef) {
      val amount = -1.9f
      controlSurfaces.!(StickBack(amount))(newController)

      expectNoMsg()
    }
  }

}

class DummyActor extends Actor {
  def receive = {
    case m =>
      throw new Exception(s"We should not have received this message $m")
  }
}