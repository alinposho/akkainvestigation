package zzz.akka.investigation.actors.in.the.cloud.pilot

import akka.actor.Actor
import akka.actor.ActorRef
import zzz.akka.investigation.actors.in.the.cloud.Plane
import zzz.akka.investigation.actors.in.the.cloud.ControlSurfaces
import zzz.akka.investigation.actors.in.the.cloud.FlyingBehaviour
import akka.actor.FSM.SubscribeTransitionCallBack
import zzz.akka.investigation.actors.in.the.cloud.altimeter.Altimeter
import akka.actor.FSM
import zzz.akka.investigation.actors.in.the.cloud.FlyingProvider

object Pilot {
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

class Pilot(plane: ActorRef,
            autopilot: ActorRef,
            heading: ActorRef,
            altimeter: ActorRef) extends Actor {
  this: DrinkingProvider with FlyingProvider =>

  import Pilot._
  import Plane._
  import context._
  import Altimeter._
  import ControlSurfaces._
  import DrinkingBehaviour._
  import FlyingBehaviour._
  import FSM._
  val copilotName = context.system.settings.config.getString(
    "zzz.akka.avionics.flightcrew.copilotName")
  override def preStart() {
    actorOf(newDrinkingBehaviour(self), "DrinkingBehaviour")
    actorOf(newFlyingBehaviour(plane, heading, altimeter), "FlyingBehaviour")
  }
  // We've pulled the bootstrapping code out into a separate receive
  // method. We'll only ever be in this state once, so there's no point
  // in having it around for long
  def bootstrap: Receive = {
    case ReadyToGo =>
      val copilot = actorFor("../" + copilotName)
      val flyer = actorFor("FlyingBehaviour")
      flyer ! SubscribeTransitionCallBack(self)
      flyer ! Fly(CourseTarget(20000, 250, System.currentTimeMillis + 30000))
      become(sober(copilot, flyer))
  }
  // The 'sober' behaviour
  def sober(copilot: ActorRef, flyer: ActorRef): Receive = {
    case FeelingSober =>
    // We're already sober
    case FeelingTipsy =>
      becomeTipsy(copilot, flyer)
    case FeelingLikeZaphod =>
      becomeZaphod(copilot, flyer)
  }
  // The 'tipsy' behaviour
  def tipsy(copilot: ActorRef, flyer: ActorRef): Receive = {
    case FeelingSober =>
      becomeSober(copilot, flyer)
    case FeelingTipsy =>
    // We're already tipsy
    case FeelingLikeZaphod =>
      becomeZaphod(copilot, flyer)
  }
  // The 'zaphod' behaviour
  def zaphod(copilot: ActorRef, flyer: ActorRef): Receive = {
    case FeelingSober =>
      becomeSober(copilot, flyer)
    case FeelingTipsy =>
      becomeTipsy(copilot, flyer)
    case FeelingLikeZaphod =>
    // We're already Zaphod
  }
  // The 'idle' state is merely the state where the Pilot does nothing at all
  def idle: Receive = {
    case _ =>

  }

  // Updates the FlyingBehaviour with sober calculations and then
  // becomes the sober behaviour
  def becomeSober(copilot: ActorRef, flyer: ActorRef) = {
    flyer ! NewElevatorCalculator(calcElevator)
    flyer ! NewBankCalculator(calcAilerons)
    become(sober(copilot, flyer))
  }
  // Updates the FlyingBehaviour with tipsy calculations and then
  // becomes the tipsy behaviour
  def becomeTipsy(copilot: ActorRef, flyer: ActorRef) = {
    flyer ! NewElevatorCalculator(tipsyCalcElevator)
    flyer ! NewBankCalculator(tipsyCalcAilerons)
    become(tipsy(copilot, flyer))
  }
  // Updates the FlyingBehaviour with zaphod calculations and then
  // becomes the zaphod behaviour
  def becomeZaphod(copilot: ActorRef, flyer: ActorRef) = {
    flyer ! NewElevatorCalculator(zaphodCalcElevator)
    flyer ! NewBankCalculator(zaphodCalcAilerons)
    become(zaphod(copilot, flyer))
  }
  // At any time, the FlyingBehaviour could go back to an Idle state,
  // which means that our behavioural changes don't matter any more
  override def unhandled(msg: Any): Unit = {
    msg match {
      case Transition(_, _, Idle) =>
        become(idle)
      // Ignore these two messages from the FSM rather than have them
      // go to the log
      case Transition(_, _, _) =>
      case CurrentState(_, _) =>
      case m => super.unhandled(m)
    }
  }
  // Initially we start in the bootstrap state
  def receive = bootstrap
}