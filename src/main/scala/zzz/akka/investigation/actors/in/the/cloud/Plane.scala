package zzz.akka.investigation.actors.in.the.cloud

import akka.actor.{ Actor, ActorLogging, ActorRef }
import akka.actor.Props
import zzz.akka.investigation.actors.in.the.cloud.pilot.CoPilot
import zzz.akka.investigation.actors.in.the.cloud.pilot.Pilot
import zzz.akka.investigation.actors.in.the.cloud.pilot.Pilots
import zzz.akka.investigation.actors.in.the.cloud.pilot.AutoPilot

object Plane {
  case object GiveMeControl
  
  val Name = "Plane"
}

class Plane extends Actor with ActorLogging {

  import Altimeter._
  import Plane._
  import EventSource.RegisterListener

  // Need to call the companion object constructor for Altimeter otherwise, an exception will be raised since
  // the class doesn't define the "eventSourceReceive" method 
  val altimeter = context.actorOf(Altimeter(), Altimeter.Name)
  val controls = context.actorOf(Props(classOf[ControlSurfaces], altimeter), ControlSurfaces.Name)
  val config = context.system.settings.config
  val pilot = context.actorOf(Props[Pilot], config.getString("zzz.akka.avionics.flightcrew.pilotName"))
  val copilot = context.actorOf(Props[CoPilot], config.getString("zzz.akka.avionics.flightcrew.copilotName"))
  val autopilot = context.actorOf(Props[AutoPilot], "AutoPilot")
  val flightAttendant = context.actorOf(LeadFlightAttendant(), config.getString("zzz.akka.avionics.flightcrew.leadAttendantName"))

  // This is deprecated. Use the code above
  // val controls = context.actorOf(Props(new ControlSurfaces(altimeter))) 

  override def preStart() {
    altimeter ! RegisterListener(self)
    List(pilot, copilot) foreach { _ ! Pilots.ReadyToGo }
  }

  def receive = {
    case GiveMeControl =>
      log.info("Plane giving control to " + sender)
      sender ! controls // Notice that it's perfectly legal to send a reference
    // in the response message
    case AltitudeUpdate(altitude) =>
      log.info(s"Altitude is now: $altitude")
  }
}