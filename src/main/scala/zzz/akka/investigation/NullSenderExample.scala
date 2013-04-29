package zzz.akka.investigation

import akka.actor.Actor
import akka.actor.ActorLogging
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.ActorSystem
import akka.actor.Props
import scala.concurrent.duration._

class EchoActor extends Actor with ActorLogging {
  def receive = {
    case any =>
      log.info(s"Received the message: $any from: $sender")
      sender ! any
  }
}

object NullSenderExample {

  def main(args: Array[String]) {

    val system = ActorSystem("NullSenderActorSystem")
    val actor = system.actorOf(Props[EchoActor])

    // Notice that no exception is raised even though the sender is null
    actor.tell("This is my message", null)

    system.scheduler.scheduleOnce(5.seconds) {
      system.shutdown
    }
  }
}