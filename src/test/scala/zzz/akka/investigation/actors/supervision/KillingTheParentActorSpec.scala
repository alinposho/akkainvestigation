package zzz.akka.investigation.actors.supervision

import akka.actor.{ Actor, ActorSystem, PoisonPill, Props }
import akka.testkit.{ ImplicitSender, TestKit, TestActorRef }
import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import org.scalatest.{ BeforeAndAfterAll, WordSpec }
import org.scalatest.junit.JUnitRunner
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class KillingTheParentActorSpec extends TestKit(ActorSystem("KillingTheParentActor"))
  with WordSpec
  with ImplicitSender
  with MustMatchers
  with BeforeAndAfterAll {

  override def afterAll() = system.shutdown()

  "ParentActor" should {
    "stop its child when it stops" in {
      val parent = TestActorRef[Parent]("parent")
      watch(parent)
      val child = parent.underlyingActor.child
      watch(child)

      parent ! PoisonPill

      expectTerminated(parent, 5 seconds)
      expectTerminated(child, 5 seconds)
    }

  }
}

class Parent extends Actor {
  val child = context.actorOf(Props[ChildActor], "child")

  def receive = {
    case _ => ???
  }
}

class ChildActor extends Actor {
  def receive = {
    case _ => ???
  }
}
