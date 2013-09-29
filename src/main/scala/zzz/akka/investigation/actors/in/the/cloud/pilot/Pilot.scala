package zzz.akka.investigation.actors.in.the.cloud.pilot

import akka.actor.Actor
import akka.actor.ActorRef
import zzz.akka.investigation.actors.in.the.cloud.Plane
import zzz.akka.investigation.actors.in.the.cloud.ControlSurfaces
import zzz.akka.investigation.actors.in.the.cloud.FlyingBehaviour

object Pilots {
  case object ReadyToGo
  case object RelinquishControl

  import FlyingBehaviour._
  import ControlSurfaces._
  // Calculates the elevator changes when we're a bit tipsy
  val tipsyCalcElevator: Calculator = { (target, status) =>
    val msg = calcElevatorChange(target, status)
    msg match {
      case StickForward(amt) => StickForward(amt * 1.03f)
      case StickBack(amt) => StickBack(amt * 1.03f)
      case m => m
    }
  }
  // Calculates the aileron changes when we're a bit tipsy
  val tipsyCalcAilerons: Calculator = { (target, status) =>
    val msg = calcAilerons(target, status)
    msg match {
      case StickLeft(amt) => StickLeft(amt * 1.03f)
      case StickRight(amt) => StickRight(amt * 1.03f)
      case m => m
    }
  }
  // Calculates the elevator changes when we're totally out of it
  val zaphodCalcElevator: Calculator = { (target, status) =>
    val msg = calcElevatorChange(target, status)
    msg match {
      case StickForward(amt) => StickBack(1f)
      case StickBack(amt) => StickForward(1f)
      case m => m
    }
  }
  // Calculates the aileron changes when we're totally out of it
  val zaphodCalcAilerons: Calculator = { (target, status) =>
    val msg = calcAilerons(target, status)
    msg match {
      case StickLeft(amt) => StickRight(1f)
      case StickRight(amt) => StickLeft(1f)
      case m => m
    }
  }

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