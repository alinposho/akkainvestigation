package zzz.akka.investigation.pingpong

import akka.testkit.TestKit
import akka.actor.ActorSystem
import akka.testkit.ImplicitSender
import scala.util.Random
import org.scalatest.WordSpec
import org.scalatest.ParallelTestExecution
import org.scalatest.matchers.MustMatchers
import akka.actor.Props
import akka.actor.ActorRef

class ActorSys(name: String) extends TestKit(ActorSystem(name))
  								with ImplicitSender
  								with DelayedInit {

  def this() = this(s"TestSystem${Random.nextInt(5)}")

  override def delayedInit(f: => Unit): Unit = {
    try {
      f
    } finally {
      shutdown()
    }
  }

  def shutdown() {
    system.shutdown
  }
}

// The example from the book does not compilex
//class MyActorSpecIsolationTest extends WordSpec
//  								with MustMatchers
//  								with ParallelTestExecution {
//  
//  import MyActor._
//
//  def makeActor(): ActorRef = system.actorOf(Props(classOf[MyActor], "MyActor"))
//
//  "My Actor" should {
//    "throw when made with the wrong name" in new ActorSys {
//      evaluating {
//        val a = system.actorOf(Props[MyActor]) // use a generated name
//      } must produce[Exception]
//    }
//    "construct without exception" in new ActorSys {
//      val a = makeActor()
//      // The throw will cause the test to fail
//    }
//    "respond with a Pong to a Ping" in new ActorSys {
//      val a = makeActor()
//      val msg = "Ping!"
//      a ! Ping(msg)
//      expectMsg(Pong(msg))
//    }
//  }
//
//}

