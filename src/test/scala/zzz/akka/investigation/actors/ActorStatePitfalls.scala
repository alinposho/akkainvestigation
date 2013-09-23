package zzz.akka.investigation.actors

import akka.actor.Actor
import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.WordSpec
import akka.testkit.ImplicitSender
import org.scalatest.matchers.MustMatchers
import org.scalatest.BeforeAndAfterAll
import akka.testkit.TestActorRef
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ActorStatePitfallsSpec extends TestKit(ActorSystem("ActorStatePitfalls"))
								with WordSpec
								with ImplicitSender
								with MustMatchers
								with BeforeAndAfterAll {
  
  override def afterAll() = system.shutdown()
  
  "ActorStatePitfalls" should {
    "not raise an exception since the code is unreachable" in {
      val actorRef = TestActorRef[ActorStatePitfalls]
      
      actorRef ! RaiseException
      
      expectMsg(RaiseException)
    }
  }
}


case object RaiseException

class ActorStatePitfalls extends Actor {
  def receive = echo orElse raiseException // The second case will never be reached and the compiler cannot complain 

  def echo: Receive = {
    case m =>
      sender ! m
  }

  def raiseException: Receive = {
    case RaiseException =>
      throw new Exception(s"Receive request to raise an exception!")
  }
}