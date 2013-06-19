package zzz.akka.investigation.restart

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.ActorRef
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

/**
 * Trait to be mixed in when one needs to print start/stop/restart information 
 * at stdout.
 */
trait RestartLogging extends Actor {
  override def preStart() = {
    println(s"The ${self.path.name} Actor started.");
  }

  override def preRestart(reason: Throwable, message: Option[Any]) = {
    super.preRestart(reason, message)
    println(s"preRestart: The ${self.path.name} Actor restarted because of: $reason.");
  }

  override def postStop() = {
    println(s"The ${self.path.name} Actor stopped.");
  }

  override def postRestart(reason: Throwable) = {
    super.postRestart(reason)
    println(s"postRestart: The ${self.path.name} Actor restarted because of: $reason.");
  }

}

class GrumpyActor extends Actor with RestartLogging {
   def receive = {
    case msg =>
      println(s"Received message $msg. Throwing exception!");
      throw new Exception("Restart this actor!")
  }
}

class Parent extends GrumpyActor {

  val child = context.actorOf(Props[GrumpyActor], "Child")

}

object ActorHierarchyRestart {
  def main(args: Array[String]) {
    val system = ActorSystem("ActorHierarchyRestart")
    val parent = system.actorOf(Props[Parent], "Parent")

    // This method will restart the parent actor and, along with it, restart the child actor too, but without calling
    // the child Actor's method hooks.
    restartActor(parent)

    system.scheduler.scheduleOnce(2 seconds) {
      system.shutdown()
    }
  }

  private def restartActor(parent: ActorRef): Unit = {
    parent ! 'SomeMessagep
  }
}



