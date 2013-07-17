package zzz.akka.investigation.actors.in.the.cloud

import akka.actor.{ Actor, ActorLogging, ActorRef }
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import zzz.akka.investigation.actors.in.the.cloud.pilot.CoPilot
import zzz.akka.investigation.actors.in.the.cloud.pilot.Pilot
import zzz.akka.investigation.actors.in.the.cloud.pilot.Pilots
import zzz.akka.investigation.actors.in.the.cloud.pilot.AutoPilot
import zzz.akka.investigation.actors.in.the.cloud.pilot.PilotProvider
import zzz.akka.investigation.actors.in.the.cloud.supervisor.IsolatedStopSupervisor
import zzz.akka.investigation.actors.in.the.cloud.supervisor.OneForOneSupervisionStrategy
import zzz.akka.investigation.actors.in.the.cloud.supervisor.IsolatedResumeSupervisor
import scala.concurrent.Await
import zzz.akka.investigation.actors.in.the.cloud.supervisor.IsolatedLifeCycleSupervisor
import zzz.akka.investigation.actors.in.the.cloud.supervisor.IsolatedStopSupervisor
import zzz.akka.investigation.actors.in.the.cloud.supervisor.IsolatedStopSupervisor
import zzz.akka.investigation.actors.in.the.cloud.flight.attendant.LeadFlightAttendantProvider
import zzz.akka.investigation.actors.in.the.cloud.altimeter.AltimeterProvider
import zzz.akka.investigation.actors.in.the.cloud.altimeter.Altimeter
import zzz.akka.investigation.actors.in.the.cloud.heading.HeadingIndicator
import zzz.akka.investigation.actors.in.the.cloud.heading.HeadingIndicatorProvider

object Plane {
  case object GiveMeControl
  case class GetPerson(name: String)
  case class PersonReference(actor: ActorRef)

  val Name = "Plane"
  val ControlsName = "Controls"
  val PilotsSupervisorName = "Pilots"
}

class ResumeSupervisor(plane: ActorRef) extends IsolatedResumeSupervisor
										  with OneForOneSupervisionStrategy
										  with AltimeterProvider
										  with HeadingIndicatorProvider
										  with PilotProvider {

  override def childStarter() {
    context.actorOf(Props(classOf[AutoPilot], plane), AutoPilot.Name)
    val alt = context.actorOf(altimeter, Altimeter.Name)
    val heading = context.actorOf(headingIndicator, HeadingIndicator.Name)
    context.actorOf(Props(classOf[ControlSurfaces], alt, heading), ControlSurfaces.Name)
  }
}

class StopSupervisor(val plane: ActorRef,
                     val autopilot: ActorRef,
                     val controls: ActorRef,
                     val altimeter: ActorRef,
                     val pilotName: String,
                     val copilotName: String)
					  extends IsolatedStopSupervisor
					  with OneForOneSupervisionStrategy
					  with PilotProvider
					  with LeadFlightAttendantProvider {

  override def childStarter() {
    context.actorOf(Props(classOf[CoPilot], plane, autopilot, altimeter), copilotName)
    context.actorOf(Props(classOf[Pilot], plane, autopilot, controls, altimeter), pilotName)
  }
}

class Plane extends Actor with ActorLogging {
  this: LeadFlightAttendantProvider with PilotProvider =>

  import Altimeter._
  import HeadingIndicator._
  import Plane._
  import EventSource.RegisterListener
  import IsolatedLifeCycleSupervisor.WaitForStart
  import Pilots._

  implicit val timeout = Timeout(5.seconds)

  val config = context.system.settings.config
  val leadFlightAttendantName = config.getString("zzz.akka.avionics.flightcrew.leadAttendantName")
  val pilotName = config.getString("zzz.akka.avionics.flightcrew.pilotName")
  val copilotName = config.getString("zzz.akka.avionics.flightcrew.copilotName")

  var controls: ActorRef = null;

  override def preStart() {
    // Get our children going. Order is important here! 
    startControls()
    startPeople()
    // Bootstrap the system
    actorForControls(Altimeter.Name) ! EventSource.RegisterListener(self)
    actorForControls(HeadingIndicator.Name) ! EventSource.RegisterListener(self)
    actorForPilots(pilotName) ! ReadyToGo
    actorForPilots(copilotName) ! ReadyToGo
    actorForPilots(AutoPilot.Name) ! ReadyToGo
  }

  private def startControls() {
    controls = context.actorOf(Props(classOf[ResumeSupervisor], self), ControlsName)
    Await.result(controls ? WaitForStart, 1 second)
  }

  private def startPeople() {
    val plane: ActorRef = self
    val controls = actorForControls(ControlSurfaces.Name)
    val autopilot = actorForControls(Pilots.AutoPilotName)
    val altimeter = actorForControls(Altimeter.Name)

    val people = context.actorOf(Props(classOf[StopSupervisor], plane, controls,
      autopilot, altimeter, pilotName, copilotName), PilotsSupervisorName)

    // Use the default strategy here, which restarts indefinitely
    context.actorOf(newLeadFlightAttendant, leadFlightAttendantName)
    Await.result(people ? WaitForStart, 5.second)
  }
  
  def actorForControls(name: String) = context.actorFor(ControlsName + "/" + name)
  def actorForPilots(name: String) = context.actorFor(PilotsSupervisorName + "/" + name)

  def receive = {
    case GiveMeControl =>
      log.info("Plane giving control to " + sender)
      sender ! actorForControls(ControlSurfaces.Name) // Notice that it's perfectly legal to send a reference
    // in the response message
    case AltitudeUpdate(altitude) =>
      log.info(s"Altitude is now: $altitude")
    case HeadingUpdate(newHeading) =>
      log.info(s"Heading is now: $newHeading")
    case GetPerson(actorName) => sender ! PersonReference(actorForPilots(actorName)); 
  }
}