package zzz.akka.investigation.actors.in.the.cloud.pilot

import akka.actor.Actor
import akka.actor.ActorRef
import Pilots._

class CoPilot(var plane: ActorRef,
               autopilot: ActorRef,
               altimeter: ActorRef) extends Actor {

  val pilotName = context.system.settings.config.getString("zzz.akka.avionics.flightcrew.pilotName")
  
  var pilot: ActorRef = context.system.deadLetters

  def receive = {
    case ReadyToGo =>
      pilot = context.actorFor("../" + pilotName)
  }

}