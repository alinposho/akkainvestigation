package fsm

import akka.actor.{ Actor, ActorRef, FSM }
import scala.concurrent.duration._

package buncher {

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

import fsm.buncher.States._
class Buncher extends Actor with FSM[State, Data] {
  
  import buncher.Events._
  
  startWith(Idle, Uninitialized)
  
  when(Idle) {
    case Event(SetTarget(ref), Uninitialized) => 
      stay using Todo(ref, Vector.empty)
  }
  
  when(Active) {
    case Event(Flush | StateTimeout, t: Todo) => 
      goto(Idle) using t.copy(queue = Vector.empty)
  }
  
  initialize()
}

object ASimpleExample {

}