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
  case class HasControl(newController: ActorRef)

  val Name = "ControlSurfaces"
}

class ControlSurfaces(plane: ActorRef,
					  altimeter: ActorRef,
                      heading: ActorRef) extends Actor {
  import ControlSurfaces._
  import Altimeter._
  import HeadingIndicator._

  override def receive() = controlledBy(context.system.deadLetters)

  def controlledBy(controller: ActorRef): Receive = {
    case StickBack(amount) if (sender == controller) =>
      altimeter ! RateChange(amount)
    case StickForward(amount) if (sender == controller) =>
      altimeter ! RateChange(-1 * amount)
    case StickLeft(amount) if (sender == controller) =>
      heading ! BankChange(-1 * amount)
    case StickRight(amount) if (sender == controller) =>
      heading ! BankChange(amount)
    case HasControl(newController) if sender == plane =>
      context.become(controlledBy(newController))
    case m => 
      throw new Exception(s"We should have not received this message ${m}")
  }
}

