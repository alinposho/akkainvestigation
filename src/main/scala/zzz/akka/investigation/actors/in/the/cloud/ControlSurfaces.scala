package zzz.akka.investigation.actors.in.the.cloud

import akka.actor.{ Actor, ActorRef }
import akka.actor.Props
import zzz.akka.investigation.actors.in.the.cloud.altimeter.Altimeter
import zzz.akka.investigation.actors.in.the.cloud.heading.HeadingIndicator

/**
 * This object carries messages for controlling the plane.
 */
object ControlSurfaces {
  case class StickBack(amount: Float)
  case class StickForward(amount: Float)
  case class StickLeft(amount: Float)
  case class StickRight(amount: Float)

  val Name = "ControlSurfaces"
}

class ControlSurfaces(altimeter: ActorRef, headingIndicator: ActorRef) extends Actor {
  import ControlSurfaces._
  import Altimeter._
  import HeadingIndicator._

  override def receive() = {
    case StickBack(amount) =>
      altimeter ! RateChange(amount)
    case StickForward(amount) =>
      altimeter ! RateChange(-1 * amount)
    case StickLeft(amount) =>
      headingIndicator ! BankChange(amount)
    case StickRight(amount) =>
      headingIndicator ! BankChange(-1 * amount)
  }
}

