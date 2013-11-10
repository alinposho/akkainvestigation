package fsm

import akka.actor.{ Actor, ActorRef, FSM }
import scala.concurrent.duration._

object Buncher {

  object Events {
    // sent
    case class SetTarget(ref: ActorRef)
    case class Queue(obj: Any)
    case object Flush

    // received
    case class Batch(seq: Seq[Any])
  }

  object States {
    sealed trait State
    case object Idle extends State
    case object Active extends State

    sealed trait Data
    case object Uninitialized extends Data
    case class Todo(target: ActorRef, queue: Seq[Any]) extends Data
  }
}

import fsm.Buncher.States._
class Buncher extends Actor with FSM[State, Data] {
  
  startWith(Idle, Uninitialized)
  
  
  initialize()
}

object ASimpleExample {

}