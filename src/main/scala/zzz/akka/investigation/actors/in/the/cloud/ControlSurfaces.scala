package zzz.akka.investigation.actors.in.the.cloud

import akka.actor.{Actor, ActorRef}
import akka.actor.Props

/**
 * This object carries messages for controlling the plane.
 */
object ControlSurfaces {
  case class StickBack(amount: Float)
  case class StickForward(amount: Float)
  
  val Name = "ControlSurfaces"
}

// Pass in the Altimeter as an ActorRef so that we can send messages to it.
class ControlSurfaces(altimeter: ActorRef) extends Actor {
  import ControlSurfaces._
  import Altimeter._
  
  override def receive() = {
    case StickBack(amount) => 
      altimeter ! RateChange(amount)
    case StickForward(amount) => 
      altimeter ! RateChange(-1 * amount)
  }
}

