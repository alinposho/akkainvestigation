package zzz.akka.investigation

import akka.actor.Actor
import akka.actor.ActorLogging
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.ActorSystem
import akka.actor.Props
import scala.concurrent.duration._

case object Push

class StackExplosion extends Actor with ActorLogging {

  def receive = push

  def push: Receive = {
    case Push =>
      log.info("Pushing state onto the stack.")
      context.become(push, discardOld = false)
  }
}

object StackExplosion {
  def main(args: Array[String]) {
    val system = ActorSystem("BlowingUpTheStack")
    val actor = system.actorOf(Props[StackExplosion])
    
    // This should eventually blow up the stack since the state changes do
    // not discard old states. 
    while(true) {
      actor ! Push
    }
    
    system.scheduler.scheduleOnce(5 minutes) {
      system.shutdown
    }
  }
}