package zzz.akka.investigation.actors.in.the.cloud.pilot

import akka.actor.Actor
import akka.actor.ActorRef
import Pilot._
import akka.dispatch.sysmsg.Terminate
import akka.actor.Terminated
import zzz.akka.investigation.actors.in.the.cloud.Plane.GiveMeControl

class CoPilot(var plane: ActorRef,
              autopilot: ActorRef,
              altimeter: ActorRef) extends Actor {

  val pilotName = context.system.settings.config.getString("zzz.akka.avionics.flightcrew.pilotName")

  var pilot: ActorRef = context.system.deadLetters
  var controls: ActorRef = _

  def receive = {
    case ReadyToGo =>
      pilot = context.actorFor("../" + pilotName)
      context.watch(pilot)
    case controlSurfaces: ActorRef =>
      controls = controlSurfaces
    case Terminated(actor) =>
      assert(actor == pilot, s"The copilot actor ${self} should not be watching the ${actor} actor")
      plane ! GiveMeControl
  }

}