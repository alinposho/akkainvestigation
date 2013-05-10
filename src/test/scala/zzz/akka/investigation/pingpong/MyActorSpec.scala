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
import akka.testkit.ImplicitSender
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

// For some unknown reason and unlike in the book, this class works, despite the 
// fact that it's run in parallel. I might need to provide a Distributor instance
// to the runTests method, but I have no idea how to do that.
@RunWith(classOf[JUnitRunner])
class MyActorSpec extends TestKit(ActorSystem("MyActorSpec"))
  							with WordSpec
  							with MustMatchers
  							with BeforeAndAfterAll
  							with ParallelTestExecution
  							with ImplicitSender {

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
      val msg = "My message"
      a ! Ping(msg)
      expectMsg(Pong(msg))
    }
  }

}