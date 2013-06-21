package zzz.akka.investigation.actors.in.the.cloud.pilot

import akka.actor.Actor
import Pilots.ReadyToGo
import zzz.akka.investigation.actors.in.the.cloud.Plane.{ GiveMeControl, GetPerson, PersonReference }
import akka.actor.ActorRef
import akka.actor.Terminated
import scala.concurrent.Await
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent.duration._

object AutoPilot {
  val Name = "Autopilot"
}

class AutoPilot(val plane: ActorRef) extends Actor {
  implicit val timeout = Timeout(5 seconds)

  val copilotName = context.system.settings.config.getString("zzz.akka.avionics.flightcrew.copilotName")
  var controls: ActorRef = _

  def receive = {
    case ReadyToGo =>
      val autopilotContext = context
      val result = Await.result(plane ? GetPerson(copilotName), 100 seconds)
      result match {
        case PersonReference(copilot) =>
          autopilotContext.watch(copilot)
        case msg => throw new Exception(s"Unexpected message $msg")
      }
    case Terminated(_) =>
      plane ! GiveMeControl
    case controlSurfaces: ActorRef =>
      controls = controlSurfaces

  }
}