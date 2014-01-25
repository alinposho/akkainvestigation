package zzz.akka.investigation

import akka.actor._
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

case class SomeMessage(msg: String)
case class DelayedResponse(msg: String)

/*
 * This is a clear example of an erroneous use of the "sender" field from an actor.
 */
class DoNotDoActor extends Actor with ActorLogging {

  def receive = {
    case SomeMessage(msg) =>
      log.info(s"Received message: $msg")
      // This will send the DelayedResponse message to the scheduler actor instead of sending it to the sender of the 
      // SomeMessage message.
      context.system.scheduler.scheduleOnce(1 seconds) {
        sender ! DelayedResponse(msg) // DO NOT DO THIS!
      }
    case DelayedResponse(msg) =>
      log.info(s"We shouldn't have received the DelayedReponse message!")
  }

}

object BreakingActorFortress {

  def main(args: Array[String]) {
	  val system = ActorSystem("DoNotDoActorSystem")
	  val actor = system.actorOf(Props[DoNotDoActor])
	
	  actor ! SomeMessage("This is a message")
	  
	  implicit val timeout = Timeout(5 seconds)
	  // This will raise an exception since no response will be returned
//	  val response = Await.result((actor ? SomeMessage("This is my message")).mapTo[DelayedResponse], 5 seconds)
	  
	  system.scheduler.scheduleOnce(6 seconds){
	    system.shutdown()
	  }
  }

}