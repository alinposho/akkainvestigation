package zzz.akka.investigation.looking.up.actors

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

class ActorThatDoesNotExist extends Actor {
  override def receive = {
    case _ =>
      val doesNotExist = context.actorSelection("user/this/actor/does/not/exist")
      if (doesNotExist == context.system.deadLetters) {
        println("Yup, it really doesn't exist")
      } else {
        println("Whoah! Someone created an actor that I thought didn't exist " +
          "while I was checking to see if it did exist.")
      }
  }
}

object ActorThatDoesNotExist {

  def main(args: Array[String]) {
    val system = ActorSystem("GenericSystem")
    val actor = system.actorOf(Props[ActorThatDoesNotExist])

    actor ! "Any message will do"

    system.scheduler.scheduleOnce(2 seconds) {
      system.shutdown()
    }
  }

}