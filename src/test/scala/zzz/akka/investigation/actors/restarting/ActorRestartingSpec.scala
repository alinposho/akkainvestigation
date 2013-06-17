package zzz.akka.investigation.actors.restarting

import org.junit.runner.RunWith
import org.scalatest.{ BeforeAndAfterAll, WordSpec }
import org.scalatest.matchers.MustMatchers
import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, Props, Terminated }
import org.scalatest.junit.JUnitRunner
import akka.actor.OneForOneStrategy
import scala.concurrent.duration._
import akka.actor.SupervisorStrategy._
import akka.testkit.{ ImplicitSender, TestActorRef, TestKit }
import Constants._
import scala.collection.concurrent.Gen
import org.scalatest.BeforeAndAfter

object Constants {
  val MaxNrOfRetries = 5;
  val Get = "get"
}

class Constants() extends Actor {

  override val supervisorStrategy = OneForOneStrategy(maxNrOfRetries = MaxNrOfRetries, withinTimeRange = 1 minute) {
    case _: ArithmeticException ⇒ Resume
    case _: NullPointerException ⇒ Restart
    case _: IllegalArgumentException ⇒ Stop
    case _: Exception ⇒ Escalate
  }

  def receive = {
    case p: Props => createChildAndSendIt(p);
  }

  private def createChildAndSendIt(p: akka.actor.Props): Unit = {
    sender ! context.actorOf(p)
  }
}

class Child extends Actor with ActorLogging {

  var state = 0
  def receive() = {
    case ex: Exception => throw ex
    case x: Int => state = x
    case Get => sender ! state
  }
}

/**
 * The example of Actor Supervision is taken from here: http://doc.akka.io/docs/akka/snapshot/scala/fault-tolerance.html
 */
@RunWith(classOf[JUnitRunner])
class ActorRestartingSpec extends TestKit(ActorSystem("ActorRestartingSpec"))
  with WordSpec
  with MustMatchers
  with ImplicitSender
  with BeforeAndAfterAll
  with BeforeAndAfter {

  var supervisor: ActorRef = _
  var child: ActorRef = _

  before {
    supervisor = system.actorOf(Props[Constants])
    child = createChild(supervisor)
  }

  def createChild(parent: ActorRef) = {
    parent ! Props[Child]
    expectMsgType[ActorRef]
  }

  override def afterAll() = system.shutdown()

  "The Actor Under Test" should {

    "not loose its state when it crashes due to ArithmeticException" in {
      val newState = 42
      setStateTo(newState, child)

      checkStateEqualTo(newState, child)

      crash(child)
      checkStateDidNotReset(child, newState)
    }

    "not restart indefinitely" in {

      watch(child)
      val times = MaxNrOfRetries;
      restart(child, times)

      expectTerminated(child, 5 seconds)
    }
  }

  def setStateTo(newState: Int, child: ActorRef) {
    child ! newState
  }

  def checkStateEqualTo(newState: Int, child: ActorRef) {
    child ! Get
    expectMsg(newState)
  }

  def crash(child: ActorRef) = child ! new ArithmeticException

  def checkStateDidNotReset(child: ActorRef, state: Int) {
    child ! Get
    expectMsg(state)
  }

  def restart(child: ActorRef, times: Int) {
    for (i <- 0 to times) {
      child ! new NullPointerException("Restart")
    }
  }

}