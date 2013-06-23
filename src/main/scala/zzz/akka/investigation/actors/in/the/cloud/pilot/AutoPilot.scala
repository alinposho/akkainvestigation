package zzz.akka.investigation.actors.in.the.cloud.pilot

import akka.actor.Actor
import Pilots.ReadyToGo
import zzz.akka.investigation.actors.in.the.cloud.Plane.{ GiveMeControl, GetPerson, PersonReference }
import akka.actor.ActorRef
import akka.actor.Terminated
import scala.concurrent.Await
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent.duration._

object AutoPilot {
  val Name = "Autopilot"
}

class AutoPilot(val plane: ActorRef) extends Actor {
  implicit val timeout = Timeout(5 seconds)

  val copilotName = context.system.settings.config.getString("zzz.akka.avionics.flightcrew.copilotName")
  var controls: ActorRef = _

  /**
   * Please be careful since there if a direct dependency between ReadyToGo,
   *  PersonReference(ref), Terminated(_), ActorRef messages. They need to arrive in 
   *  this specific order for the actor to work.
   */
  def receive = {
    case ReadyToGo =>
      plane ! GetPerson(copilotName)
    case PersonReference(copilotRef) =>
      context.watch(copilotRef)
    case Terminated(_) =>
      plane ! GiveMeControl
    case controlSurfaces: ActorRef =>
      controls = controlSurfaces

  }
}