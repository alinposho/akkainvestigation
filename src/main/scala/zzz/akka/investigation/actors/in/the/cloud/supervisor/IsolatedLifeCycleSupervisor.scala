package zzz.akka.investigation.actors.in.the.cloud.supervisor

import akka.actor.Actor

/**
 * Defines messages exchanged with actors waiting for this actor to finish
 * starting.
 */
object IsolatedLifeCycleSupervisor {
  case object WaitForStart
  case object Started
}

trait IsolatedLifeCycleSupervisor extends Actor {
  import IsolatedLifeCycleSupervisor._

  def receive = {
    case WaitForStart =>
      println("Sender: " + sender + "\nSender type: " + sender.getClass());
      sender ! Started
    case msg =>
      throw new Exception(s"Don't call ${self.path.name} directly (${msg}).")
  }

  def childStarter(): Unit

  final override def preStart() { childStarter() }

  final override def preRestart(reason: Throwable, message: Option[Any]) {}
  final override def postRestart(reason: Throwable) {}
  
}