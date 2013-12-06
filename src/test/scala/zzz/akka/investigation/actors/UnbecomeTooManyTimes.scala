package zzz.akka.investigation.actors

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem }
import akka.testkit.{ ImplicitSender, TestKit, TestActorRef }
import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import org.scalatest.{ BeforeAndAfterAll, WordSpec }
import org.scalatest.junit.JUnitRunner
import scala.concurrent.duration._

@RunWith(classOf[JUnitRunner])
class UnbecomeTooManyTimes extends TestKit(ActorSystem("ActorStatePitfalls"))
  with WordSpec
  with ImplicitSender
  with MustMatchers
  with BeforeAndAfterAll {

  override def afterAll() = system.shutdown()

  "UnbecomeActor" should {
    "not raise an exception when calling unbecome without calling previously calling become" in {
      val actorRef = TestActorRef[UnbecomeActor]
      watch(actorRef)// Watch the actor. Maybe it will throw and exception and dye when calling unbecome...
      
      actorRef ! 'unbecome
      
      asserBehaviourDidNotChangeAndActorEchoesMessages(actorRef)
    }

    def asserBehaviourDidNotChangeAndActorEchoesMessages(actorRef: ActorRef): Unit = {
      val message = "blah"
      actorRef ! message

      expectMsg(message)
    }

  }
}

class UnbecomeActor extends Actor with ActorLogging {
  def receive() = {
    case 'unbecome => context.unbecome() // It would seem that this does not kill the actor despite not having any state in the stack to pop.
    case e => sender ! e
  }
}
