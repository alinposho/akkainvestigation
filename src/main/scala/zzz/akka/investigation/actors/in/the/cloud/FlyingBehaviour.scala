package zzz.akka.investigation.actors.in.the.cloud

import akka.actor.ActorRef
import akka.actor.Actor
import akka.actor.FSM
import zzz.akka.investigation.actors.in.the.cloud.pilot.Pilots
import zzz.akka.investigation.actors.in.the.cloud.altimeter.Altimeter
import zzz.akka.investigation.actors.in.the.cloud.heading.HeadingIndicator

object FlyingBehaviour {
  import ControlSurfaces._

  // The states governing behavioral transitions
  sealed trait State
  case object Idle extends State
  case object Flying extends State
  case object PreparingToFly extends State

  case class CourseTarget(altitude: Double, heading: Float, byMillis: Long)
  case class CourseStatus(altitude: Double, heading: Float, headingSinceMS: Long, altitudeSinceMS: Long)

  type Calculator = (CourseTarget, CourseStatus) => Any

  sealed trait Data
  case object Uninitialized extends Data
  case class FlightData(controls: ActorRef,
                        elevCalculator: Calculator,
                        bankCalculator: Calculator,
                        target: CourseTarget,
                        status: CourseStatus) extends Data

  case class Fly(target: CourseTarget)

  def currentMS = System.currentTimeMillis()
  
  def calcElevatorChange(target: CourseTarget, status: CourseStatus): Any = {
    val alt = (target.altitude - status.altitude).toFloat
    val dur = target.byMillis - status.altitudeSinceMS
    if (alt < 0) StickForward((alt / dur) * -1)
    else StickBack(alt / dur)
  }

  def calcAilerons(target: CourseTarget, status: CourseStatus): Any = {
    import scala.math.{ abs, signum }

    val diff = target.heading - status.heading
    val dur = target.byMillis - status.headingSinceMS
    val amount = if (abs(diff) < 180) diff
    else signum(diff) * (abs(diff) - 360f)

    if (amount > 0) StickRight(amount / dur)
    else StickLeft((amount / dur) * -1)
  }
}

class FlyingBehaviour(plane: ActorRef,
                      heading: ActorRef,
                      altimeter: ActorRef) extends Actor
                      					   with FSM[FlyingBehaviour.State, FlyingBehaviour.Data] {
  import FSM._
  import FlyingBehaviour._
  import Pilots._
  import Plane._
  import Altimeter._
  import HeadingIndicator._
  import EventSource._
  import ControlSurfaces._
  import scala.concurrent.duration._

  case object Adjust

  startWith(Idle, Uninitialized)
  
  def adjust(flightData: FlightData): FlightData = {
    val FlightData(c, elevCalc, bankCalc, t, s) = flightData
    c ! elevCalc(t, s)
    c ! bankCalc(t, s)
    flightData
  }
  
  when(Idle) {
    case Event(Fly(target), _) =>
      goto(PreparingToFly) using FlightData(context.system.deadLetters,
        calcElevatorChange,
        calcAilerons,
        target,
        CourseStatus(-1, -1, 0, 0))
  }

  onTransition {
    case Idle -> PreparingToFly =>
      plane ! GiveMeControl
      heading ! RegisterListener(self)
      altimeter ! RegisterListener(self)
  }
  
  def prepComplete(data: Data) = {
    data match {
      case FlightData(c, _, _, _, s) =>
        if (c != context.system.deadLetters &&
          s.heading != -1f && s.altitude != -1f)
          true
        else
          false
      case _ =>
        false
    }
  }
  
  when(PreparingToFly)(transform {
    case Event(HeadingUpdate(head), d: FlightData) =>
      stay using d.copy(status = d.status.copy(heading = head,
        headingSinceMS = currentMS))
    case Event(AltitudeUpdate(alt), d: FlightData) =>
      stay using d.copy(status = d.status.copy(altitude = alt,
        altitudeSinceMS = currentMS))
    case Event(Controls(ctrls), d: FlightData) =>
      stay using d.copy(controls = ctrls)
    case Event(StateTimeout, _) =>
      plane ! LostControl
      goto(Idle)
  } using {
    case s if prepComplete(s.stateData) =>
      s.copy(stateName = Flying)
  })
  
  when(Flying) {
    case Event(Adjust, flightData: FlightData) =>
      stay using adjust(flightData)
    case Event(AltitudeUpdate(alt), d: FlightData) =>
      stay using d.copy(status = d.status.copy(altitude = alt,
        altitudeSinceMS = currentMS))
    case Event(HeadingUpdate(head), d: FlightData) =>
      stay using d.copy(status = d.status.copy(heading = head,
        headingSinceMS = currentMS))
  }

  onTransition {
    case PreparingToFly -> Flying =>
      setTimer("Adjustment", Adjust, 200 milliseconds, repeat = true)
  }

  onTransition {
    case _ -> Idle =>
      heading ! UnregisterListener(self)
      altimeter ! UnregisterListener(self)
  }

  whenUnhandled {
    case Event(RelinquishControl, _) =>
      goto(Idle)
  }

  initialize
}