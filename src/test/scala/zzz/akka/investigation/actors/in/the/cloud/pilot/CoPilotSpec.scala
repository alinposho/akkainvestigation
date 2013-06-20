package zzz.akka.investigation.actors.in.the.cloud.pilot

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import akka.testkit.TestKit
import akka.actor.ActorSystem
import akka.testkit.ImplicitSender
import org.scalatest.matchers.MustMatchers
import org.scalatest._
import akka.actor._
import CoPilotSpec._
import zzz.akka.investigation.actors.in.the.cloud.supervisor._
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.ask
import zzz.akka.investigation.actors.in.the.cloud.Plane._
import com.typesafe.config.ConfigFactory

class FakePilot extends Actor {
  override def receive = {
    case _ => throw new Exception("This exception is expected.")
  }
}

class NilActor extends Actor {
  def receive = {
    case _ => 
  }
}

object CoPilotSpec {
  val copilotName = "Mary"
  val pilotName = "Mark"
  val configStr = s"""
  					zzz.akka.avionics.flightcrew.copilotName = "$copilotName"
  					zzz.akka.avionics.flightcrew.pilotName = "$pilotName"
  					"""
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

@RunWith(classOf[JUnitRunner])
class CoPilotSpec extends TestKit(ActorSystem("CopilotSpec", ConfigFactory.parseString(CoPilotSpec.configStr))) 
					with MustMatchers
					with ImplicitSender
					with WordSpec 
					with BeforeAndAfterAll {

  val pilotPath = s"/user/TestPilots/$pilotName"
  val copilotPath = s"/user/TestPilots/$copilotName"
  
  override def afterAll = system.shutdown()
  
  def pilotsReadyToGo(): ActorRef = {
	implicit val askTimeout = Timeout(4 seconds)
	val supervisor = system.actorOf(Props(classOf[PilotsSupervisor], testActor), "TestPilots")
	Await.result(supervisor ? IsolatedLifeCycleSupervisor.WaitForStart, 3 seconds)
	
	system.actorFor(copilotPath) ! Pilots.ReadyToGo

	supervisor
  }
    
  "Copilot" should {
    "take control of the plane when the Pilot dies" in {
      pilotsReadyToGo()
      
      killPilot()
      
      makeSureCopilotTookControlOfThePlane()
    }
  }

  def killPilot() {
    system.actorFor(pilotPath) ! "throw"
  }

  def makeSureCopilotTookControlOfThePlane() {
    expectMsg(GiveMeControl)
    // The girl who sent it had better be Mary
    lastSender must be(system.actorFor(copilotPath))
  }

}