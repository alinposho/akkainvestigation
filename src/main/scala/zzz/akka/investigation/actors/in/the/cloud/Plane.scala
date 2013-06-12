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

object Plane {
  case object GiveMeControl

  val Name = "Plane"
  val Controls = "Controls"
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

class Plane extends Actor with ActorLogging {
  this: LeadFlightAttendantProvider with PilotProvider =>

  import Altimeter._
  import Plane._
  import EventSource.RegisterListener
  import IsolatedLifeCycleSupervisor.WaitForStart

  implicit val timeout = Timeout(5.seconds)

  val config = context.system.settings.config
  val LeadFlightAttendantName = config.getString("zzz.akka.avionics.flightcrew.leadAttendantName")
  val PilotName = config.getString("zzz.akka.avionics.flightcrew.pilotName")
  val CopilotName = config.getString("zzz.akka.avionics.flightcrew.copilotName")

  var controls: ActorRef = null;

  override def preStart() {
    startControls()
    startPeople()
  }

  private def startControls() {
    controls = context.actorOf(Props[ResumeSupervisor], Controls)
    Await.result(controls ? WaitForStart, 1 second)
  }

  def actorForControls(name: String) = context.actorFor("Controls/" + name)

  private def startPeople() {
    val plane = this
    val controls = actorForControls("ControlSurfaces")
    val autopilot = actorForControls("AutoPilot")
    val altimeter = actorForControls("Altimeter")
    val people = context.actorOf(Props(new IsolatedStopSupervisor with OneForOneSupervisionStrategy {
      def childStarter() {
        context.actorOf(Props(classOf[CoPilot], plane, autopilot, altimeter))
        context.actorOf(Props(classOf[Pilot], plane, autopilot, controls, altimeter), PilotName)
      }
    }), "Pilots")
    // Use the default strategy here, which
    // restarts indefinitely
    context.actorOf(newLeadFlightAttendant, LeadFlightAttendantName)
    Await.result(people ? WaitForStart, 1.second)
    Await.result(people ? WaitForStart, 5.second)

    context.actorOf(newLeadFlightAttendant, LeadFlightAttendantName)
  }

  def receive = {
    case GiveMeControl =>
      log.info("Plane giving control to " + sender)
    //      sender ! controls // Notice that it's perfectly legal to send a reference
    // in the response message
    case AltitudeUpdate(altitude) =>
      log.info(s"Altitude is now: $altitude")
  }
}