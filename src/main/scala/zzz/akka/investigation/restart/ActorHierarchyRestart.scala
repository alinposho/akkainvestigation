package zzz.akka.investigation.restart

import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.ActorRef
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class Parent extends Actor {

  val child = context.actorOf(Props[Child], "Child")

  override def preStart() = {
    println("The Parent Actor started.");
  }

  override def preRestart(reason: Throwable, message: Option[Any]) = {
    super.preRestart(reason, message)
    println(s"preRestart: The Parent Actor restarted because of: $reason.");
  }

  override def postStop() = {
    println("The Parent Actor stopped.");
  }

  override def postRestart(reason: Throwable) = {
    super.postRestart(reason)
    println(s"postRestart: The Parent Actor restarted because of: $reason.");
  }

  def receive = {
    case msg =>
      println(s"Received message $msg. Throwing exception!");
      throw new Exception("Restart this actor!")
  }

}

class Child extends Actor {

  override def preStart() = {
    println("The Child Actor started.");
  }

  override def preRestart(reason: Throwable, message: Option[Any]) = {
    super.preRestart(reason, message)
    println(s"preRestart: The Child Actor restarted because of: $reason.");
    postStop()
  }

  override def postStop() = {
    println("The Child Actor stopped.");
  }

  override def postRestart(reason: Throwable) = {
    super.postRestart(reason)
    println(s"postRestart: The Child Actor restarted because of: $reason.");
  }

  def receive = {
    case msg =>
      println(s"Received message $msg. Throwing exception!");
      throw new Exception("Restart this actor!")
  }

}

object ActorHierarchyRestart {
  def main(args: Array[String]) {
    val system = ActorSystem("ActorHierarchyRestart")
    val parent = system.actorOf(Props[Parent], "Parent")

    // This method will restart the parent actor and, along with it, restart the child actor too, but without callling
    // the child Actor's method hooks.
    restartActor(parent)

    system.scheduler.scheduleOnce(5 seconds) {
      system.shutdown()
    }
  }

  private def restartActor(parent: ActorRef): Unit = {
    parent ! 'SomeMessagep
  }
}



