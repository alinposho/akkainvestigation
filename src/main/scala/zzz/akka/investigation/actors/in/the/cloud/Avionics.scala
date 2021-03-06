package zzz.akka.investigation.actors.in.the.cloud

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.duration.FiniteDuration
import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Cancellable
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.pattern.ask
import akka.util.Timeout
import zzz.akka.investigation.actors.in.the.cloud.flight.attendant.LeadFlightAttendantProvider
import zzz.akka.investigation.actors.in.the.cloud.pilot.PilotProvider
import zzz.akka.investigation.actors.in.the.cloud.altimeter.Altimeter
import zzz.akka.investigation.actors.in.the.cloud.heading.HeadingIndicator

class PlaneWithFlightAttendantProvider extends Plane with LeadFlightAttendantProvider with PilotProvider

object Avionics {

  // Needed for '?' bellow
  implicit val timeout = Timeout(5.seconds)
  val system = ActorSystem("PlaneSymulation")
  val plane = system.actorOf(Props[PlaneWithFlightAttendantProvider], Plane.Name)

  // Remember the construct used to wait for an Actor's response  
  val control = Await.result((plane ? Plane.GiveMeControl).mapTo[ActorRef], 5 seconds)

  def main(args: Array[String]) {

    takeoff()
    moveLeft()
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
  
  private def moveLeft() = {
    system.scheduler.scheduleOnce(200.milli) {
      control ! ControlSurfaces.StickLeft(HeadingIndicator.MaxRateOfBank)
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