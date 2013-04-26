package zzz.akka.investigation.actors.in.the.cloud

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
// The duration package  objects extends Ints with some timing functionality
import scala.concurrent.duration.DurationInt
import scala.concurrent.ExecutionContext.global

class Altimeter extends Actor with ActorLogging {

  override def receive = {
    case msg =>
      throw new RuntimeException("Unknown message received: " + msg);
  }
}