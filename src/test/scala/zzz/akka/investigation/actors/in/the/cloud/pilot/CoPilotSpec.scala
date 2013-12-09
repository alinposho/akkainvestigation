package zzz.akka.investigation.actors.in.the.cloud.pilot

import akka.actor.{ Actor, ActorRef, ActorSystem, PoisonPill, Props }
import akka.pattern.ask
import akka.testkit.{ ImplicitSender, TestKit, TestActorRef }
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import org.scalatest.{ BeforeAndAfterAll, WordSpec }
import org.scalatest.junit.JUnitRunner
import scala.concurrent.duration._
import scala.concurrent.Await

import PilotsSpecConfig._
import zzz.akka.investigation.actors.in.the.cloud.supervisor._
import zzz.akka.investigation.actors.in.the.cloud.Plane._



@RunWith(classOf[JUnitRunner])
class CoPilotSpec extends TestKit(ActorSystem("CopilotSpec", ConfigFactory.parseString(PilotsSpecConfig.configStr))) 
					with MustMatchers
					with ImplicitSender
					with WordSpec 
					with BeforeAndAfterAll {

  
  val pilotPath = s"/user/TestPilots/$pilotName"
  val copilotPath = s"/user/TestPilots/$copilotName"
  val TestPilotsSupervisorName = "TestPilots"
  
  override def afterAll = system.shutdown()
  
  "Copilot" should {
    "take control of the plane when the Pilot dies" in {
      pilotsReadyToGo()
      
      killPilot()
      
      makeSureCopilotTookControlOfThePlane()
    }
  }

  def pilotsReadyToGo(): ActorRef = {
    val supervisor = system.actorOf(Props(classOf[PilotsSupervisor], testActor), TestPilotsSupervisorName)
    waitForSupervisorAndChildrenSetUp(supervisor)
    system.actorFor(copilotPath) ! Pilot.ReadyToGo

    supervisor
  }

  private def waitForSupervisorAndChildrenSetUp(supervisor: akka.actor.ActorRef): Any = {
    implicit val askTimeout = Timeout(4 seconds)
    Await.result(supervisor ? IsolatedLifeCycleSupervisor.WaitForStart, 3 seconds)
  }
    

  def killPilot() {
    system.actorFor(pilotPath) ! "throw"
  }

  def makeSureCopilotTookControlOfThePlane() {
    expectMsg(GiveMeControl)
    lastSender must be(system.actorFor(copilotPath))
  }

}

class FakePilot extends Actor {
  override def receive = {
    case _ => throw new Exception("This exception is expected.")
  }
}

class PilotsSupervisor(testActor: ActorRef) 
						extends IsolatedStopSupervisor
  						with OneForOneSupervisionStrategy {

  def nilActor = context.actorOf(Props[NilActor])

  def childStarter() {
    context.actorOf(Props[FakePilot], pilotName)
    context.actorOf(Props(classOf[CoPilot], testActor, nilActor, nilActor), copilotName)
  }
}
