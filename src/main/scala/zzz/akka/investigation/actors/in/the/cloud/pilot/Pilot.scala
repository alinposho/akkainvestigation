package zzz.akka.investigation.actors.in.the.cloud.pilot

import akka.actor.Actor
import akka.actor.ActorRef
import zzz.akka.investigation.actors.in.the.cloud.Plane

object Pilots {
  case object ReadyToGo
  case object RelinquishControl

  val AutoPilotName = "AutoPilot"
}

class Pilot(var plane: ActorRef,
            autopilot: ActorRef,
            var controls: ActorRef,
            altimeter: ActorRef) extends Actor {

  import Pilots._
  import Plane._

  var copilot: ActorRef = context.system.deadLetters

  val copilotName = context.system.settings.config.getString("zzz.akka.avionics.flightcrew.copilotName")

  def receive = {
    case ReadyToGo =>
      plane ! GiveMeControl
      copilot = context.actorFor("../" + copilotName)
    case controlSurfaces: ActorRef =>
      controls = controlSurfaces
  }

}