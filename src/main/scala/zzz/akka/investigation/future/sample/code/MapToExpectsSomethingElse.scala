package zzz.akka.investigation.future.sample.code

import akka.actor.{ Actor, ActorSystem }
import scala.concurrent.ExecutionContext.Implicits.global
import akka.pattern.ask
import scala.concurrent.Await
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor.Props

case class Message(msg: String)

class MisbehavingActor extends Actor {

  def receive = {
    case msg =>
      sender ! "This is my message to you"
  }
}

object MapToExpectsSomethingElse {

  def main(args: Array[String]) {
    implicit val timeout = Timeout(5.seconds)

    // Having spaces in the ActorSystem's name won't work. It will raise an
    // exception at runtime.
    //    val system = ActorSystem("My Actor system") // this won't work. It will raise 

    val system = ActorSystem("MyActorSystem")
    val misbehavingActor = system.actorOf(Props[MisbehavingActor])
    try {
      // This will raise an exception since the resulting message is not of type Message 
      val result: Message = Await.result((misbehavingActor ? "Give me smth").mapTo[Message], 5.seconds)
      println("Message returned by the actor: " + result);
    } finally { // This block is necessary to ensure graceful system shutdown
      // even in case of an exception being raised. 
      system.shutdown
    }

  }

}