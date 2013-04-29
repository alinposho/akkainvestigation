package zzz.akka.investigation.actors.in.the.cloud

import akka.actor.{ Actor, ActorRef }

object EventSource {

  case class RegisterListener(listener: ActorRef)
  case class UnregisterListener(listener: ActorRef)
}

trait EventSource { this: Actor =>
  import EventSource._

  var listeners = Vector[ActorRef]()

  def sendEvent[T](event: T): Unit = listeners foreach (_ ! event)

  def eventSourceReceive: Receive = {
    case RegisterListener(listener) =>
      listeners = listeners :+ listener 
    case UnregisterListener(listener) =>
      listeners = listeners filter (_ != listener)
  }
}