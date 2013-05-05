package zzz.akka.investigation.pingpong

import MyActor._
import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.matchers.MustMatchers
import org.scalatest.WordSpec
import org.scalatest.BeforeAndAfterAll
import org.scalatest.ParallelTestExecution
import akka.actor.ActorRef
import akka.actor.Props

class MyActorSpec extends TestKit(ActorSystem("MyActorSpec"))
  with WordSpec
  with MustMatchers
  with BeforeAndAfterAll
  with ParallelTestExecution {
  
  import MyActor._

  override def afterAll() { system.shutdown() }
  def makeActor(): ActorRef = system.actorOf(Props(classOf[MyActor], "MyActor"))

  "My Actor" should {
    "throw an exception if it's constructed with the wrong name" in {
      evaluating {
        val a = system.actorOf(Props[MyActor]) // use a generated name
      } must produce[Exception]
    }

    "construct without exception" in {
      val a = makeActor()
      // The throw will cause the test to fail
    }

    "respond with a Pong to a Ping" in {
      val a = makeActor()
      a ! Ping
      expectMsg(Pong)
    }
  }

}