package zzz.akka.investigation.actors.in.the.cloud

import akka.actor.{ Actor, ActorRef, ActorSystem, Cancellable, Props }
import akka.pattern.ask
import scala.concurrent.Await
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Avionics {

  // Needed for '?' bellow
  implicit val timeout = Timeout(5.seconds)
  val system = ActorSystem("PlaneSymulation")
  val plane = system.actorOf(Props[Plane])

  // Remember the construct used to wait for an Actor's reponse  
  val control = Await.result((plane ? Plane.GiveMeControl).mapTo[ActorRef], 5 seconds)

  def main(args: Array[String]) {

    takeoff()
    levelOut(1.seconds)
    climb()
    levelOut(4.seconds)
    shutDown()
  }

  private def takeoff(): Cancellable = {
    system.scheduler.scheduleOnce(200.milli) {
      control ! ControlSurfaces.StickBack(Altimeter.MaxAmount)
    }
  }

  private def levelOut(duration: FiniteDuration): Unit = {
    system.scheduler.scheduleOnce(duration) {
      control ! ControlSurfaces.StickBack(0f)
    }
  }

  private def climb(): Unit = {
    system.scheduler.scheduleOnce(3.second) {
      control ! ControlSurfaces.StickBack(0.5f)
    }
  }

  private def shutDown() {
    system.scheduler.scheduleOnce(5.second) {
      system.shutdown()
    }
  }
}