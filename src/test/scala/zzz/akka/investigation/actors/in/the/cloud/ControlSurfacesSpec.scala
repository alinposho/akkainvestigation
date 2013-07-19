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

@RunWith(classOf[JUnitRunner])
class ControlSurfacesSpec extends TestKit(ActorSystem("ControlSurfacesSpec"))
							with WordSpec
							with MustMatchers
							with ImplicitSender
							with BeforeAndAfterAll {
  import ControlSurfaces._
  import Altimeter._
  
  override def afterAll() = system.shutdown()
  
  "ControlSurfaces" should {
    "change the controller when the plane asks" in {
      val plane = TestActorRef[DummyActor]("plane")
      val controlSurfaces = TestActorRef[ControlSurfaces](Props(classOf[ControlSurfaces], plane, testActor, testActor))
      val newController = TestActorRef[DummyActor]("newController")

      controlSurfaces ! (HasControl(newController), plane)
      
      assertControllerWasChangedTo(newController, controlSurfaces)
    }
    
    def assertControllerWasChangedTo(newController: ActorRef, controlSurfaces: ActorRef) {
      val amount =  89898.9f
      controlSurfaces ! (StickBack(amount), newController)
      
      expectMsg(RateChange(amount))
    }
  }

}

class DummyActor extends Actor {
  def receive = {
    case m => 
      throw new Exception(s"We should not have received this message $m")
  }
}