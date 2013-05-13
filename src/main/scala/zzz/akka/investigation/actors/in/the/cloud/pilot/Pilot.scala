package zzz.akka.investigation.actors.in.the.cloud.pilot

import akka.actor.Actor
import akka.actor.ActorRef
import zzz.akka.investigation.actors.in.the.cloud.Plane

object Pilots {
  case object ReadyToGo
  case object RelinquishControl
}

class Pilot extends Actor {

  import Pilots._
  import Plane._

  var controls: ActorRef = context.system.deadLetters
  var copilot: ActorRef = context.system.deadLetters
  var autopilot: ActorRef = context.system.deadLetters
  var parent: ActorRef = context.system.deadLetters

  val copilotName = context.system.settings.config.getString("zzz.akka.avionics.flightcrew.copilotName")
  val AutopilotName = "Autopilot"
  
  override def preStart() {
    parent = context.parent
  }  
    
  def receive = {
    case ReadyToGo => 
      parent ! GiveMeControl
      copilot = context.actorFor("../" + copilotName)
      autopilot = context.actorFor("../" + AutopilotName)
    case controlSurfaces: ActorRef => 
      controls = controlSurfaces       
  } 

}