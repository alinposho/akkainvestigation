package zzz.akka.investigation

import akka.actor.{ Actor, Props, ActorSystem }
import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

case class Hello(greeting: String)
case class Goodbye(greeting: String)

class ActorWithChangingBehaviour extends Actor {

  def receive = expectHello
  
  def expectHello: Receive = {
    case Hello(greeting) =>
      sender ! Hello(greeting + " to you too!")
      context.become(expectGoodbye)
    case Goodbye(_) =>
      sender ! "Huh! Who are you?"
  }

  def expectGoodbye: Receive = {
    case Hello(_) =>
      sender ! "We've already done that?"

    case Goodbye(_) =>
      sender ! Goodbye("So long, dood!")
      context.become(expectHello)
  }

}

object ActorWithChangingBehaviour {
  def main(args: Array[String]) {
    
    val system = ActorSystem("ActorWithChangingBehaviour")
    val actor = system.actorOf(Props[ActorWithChangingBehaviour])
    
    implicit val timeout = Timeout(5 seconds)
    // prints: Huh? Who are you?
    println(Await.result(actor ? Goodbye("So long"), 1.second))
    // prints: Hello(Hithere to you too!)
    println(Await.result(actor ? Hello("Hithere"), 1.second))
    // prints: We've already done that.
    println(Await.result(actor ? Hello("Hithere again"), 1.second))
    // prints: Goodbye(So long, dood!)
    println(Await.result(actor ? Goodbye("So long"), 1.second))

    system.shutdown()
  }
}