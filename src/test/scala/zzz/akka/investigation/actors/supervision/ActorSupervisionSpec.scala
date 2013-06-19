package zzz.akka.investigation.actors.supervision

import scala.concurrent.duration._
import org.junit.runner.RunWith
import org.scalatest._
import Supervisor._
import akka.actor._
import akka.actor.SupervisorStrategy._
import akka.testkit.ImplicitSender
import akka.testkit.TestKit
import org.scalatest.matchers.MustMatchers
import akka.actor.Terminated
import akka.actor.actorRef2Scala
import org.scalatest.junit.JUnitRunner

object Supervisor {
  val MaxNrOfRetries = 5;
  val DefaultState = 0;
  val Get = "get"
}

class Supervisor() extends Actor {

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

class IsolatedSupervisor extends Actor {
  override val supervisorStrategy = OneForOneStrategy(2, 1 minute) {
    case _: Exception => Escalate
  }

  override def preRestart(reason: Throwable, message: Option[Any]) { /* suppress child termination */ }

  def receive = {
    case p: Props => sender ! context.actorOf(p)
  }
}

class Child extends Actor with ActorLogging {

  var state = DefaultState
  
  override def preRestart(reason: Throwable, message: Option[Any]){
    println(s"Child actor ${self} will restart!");
    super.preRestart(reason, message)
  }
  
  def receive() = {
    case ex: Exception => throw ex
    case x: Int => state = x
    case Get => sender ! state
  }
}

/**
 * This example of Actor Supervision is taken from here: http://doc.akka.io/docs/akka/snapshot/scala/fault-tolerance.html
 */
@RunWith(classOf[JUnitRunner])
class ActorSupervisionSpec extends TestKit(ActorSystem("ActorSpec"))
  with WordSpec
  with MustMatchers
  with ImplicitSender
  with BeforeAndAfterAll
  with BeforeAndAfter {

  var supervisor: ActorRef = _
  var child: ActorRef =
    _

  before {
    supervisor = system.actorOf(Props[Supervisor])
    child = createChild(supervisor)
  }

  def createChild(parent: ActorRef) = {
    parent ! Props[Child]
    expectMsgType[ActorRef]
  }

  override def afterAll() = system.shutdown()

  "The Child actor" should {
    "not restart indefinitely" in {

      watch(child)
      val times = MaxNrOfRetries;
      restart(child, times)

      expectTerminated(child, 5 seconds)
    }

    "not loose its state when it crashes due to ArithmeticException" in {
      val newState = 42
      setStateTo(newState, child)

      checkStateEqualTo(newState, child)

      crash(child)
      checkStateDidNotReset(child, newState)
    }

    "restart when it crashes due to NullPointerException" in {
      val newState = 42
      setStateTo(newState, child)

      seriouslyCrash(child)

      checkChildStateWasResetDueToRestart(child)
    }

    "be terminated by its supervisor due to IllegalArgumentException" in {
      watch(child)

      causeTerminationOf(child)

      expectTerminated(child, 5 seconds)
    }
  }

  def restart(child: ActorRef, times: Int) {
    for (i <- 0 to times) yield {
      child ! new NullPointerException("Restart")
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

  def seriouslyCrash(actor: ActorRef) = actor ! new NullPointerException

  def checkChildStateWasResetDueToRestart(child: ActorRef) {
    child ! Get
    expectMsg(DefaultState)
  }

  def causeTerminationOf(actor: ActorRef) = actor ! new IllegalArgumentException

  "The Supervisor actor" should {
    "escalate the Exception to its parent and terminate its children" in {
      watch(child)

      verifyIsAlive(child)

      crashWithException(child)
      expectMsgPF() {
        case t @ Terminated(child) if t.existenceConfirmed ⇒ ()
      }
    }

    def verifyIsAlive(actor: ActorRef) {
      child ! Get
      expectMsg(DefaultState)
    }
  }

  def crashWithException(actor: ActorRef) = {
    actor ! new Exception("CRASH") // escalate failure
  }

  "Isolated supervisor actor" should {
    "restart its children when restarting" in {
      
      val isolatedSupervisor = system.actorOf(Props[IsolatedSupervisor])
      val child = createChild(isolatedSupervisor)
      watch(child);

      val newState = 42
      setStateTo(newState, child)

      crashWithException(child)
      checkChildStateWasResetDueToRestart(child)
    }
  }

}