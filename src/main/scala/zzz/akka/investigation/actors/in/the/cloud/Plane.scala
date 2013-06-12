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

object Plane {
  case object GiveMeControl

  val Name = "Plane"
  val ControlsName = "Controls"
  val PilotsSupervisorName = "Pilots"
}

class ResumeSupervisor extends IsolatedResumeSupervisor
  with OneForOneSupervisionStrategy
  with PilotProvider
  with AltimeterProvider {

  override def childStarter() {
    context.actorOf(newAutopilot, AutoPilot.Name)
    val alt = context.actorOf(altimeter, Altimeter.Name)
    context.actorOf(Props(classOf[ControlSurfaces], alt), ControlSurfaces.Name)
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
  import Plane._
  import EventSource.RegisterListener
  import IsolatedLifeCycleSupervisor.WaitForStart

  implicit val timeout = Timeout(5.seconds)

  val config = context.system.settings.config
  val LeadFlightAttendantName = config.getString("zzz.akka.avionics.flightcrew.leadAttendantName")
  val pilotName = config.getString("zzz.akka.avionics.flightcrew.pilotName")
  val copilotName = config.getString("zzz.akka.avionics.flightcrew.copilotName")

  var controls: ActorRef = null;

  override def preStart() {
    // Get our children going. Order is important here! 
    startControls()
    startPeople()
    // Bootstrap the system
    actorForControls(Altimeter.Name) ! EventSource.RegisterListener(self)
    actorForPilots(pilotName) ! Pilots.ReadyToGo
    actorForPilots(copilotName) ! Pilots.ReadyToGo
  }

  def actorForControls(name: String) = context.actorFor(ControlsName + "/" + name)
  // Helps us look up Actors within the "Pilots" Supervisor
  def actorForPilots(name: String) = context.actorFor(PilotsSupervisorName + "/" + name)

  private def startControls() {
    controls = context.actorOf(Props[ResumeSupervisor], ControlsName)
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
    context.actorOf(newLeadFlightAttendant, LeadFlightAttendantName)
    Await.result(people ? WaitForStart, 5.second)

  }

  def receive = {
    case GiveMeControl =>
      log.info("Plane giving control to " + sender)
      sender ! actorForControls(ControlSurfaces.Name) // Notice that it's perfectly legal to send a reference
    // in the response message
    case AltitudeUpdate(altitude) =>
      log.info(s"Altitude is now: $altitude")
  }
}