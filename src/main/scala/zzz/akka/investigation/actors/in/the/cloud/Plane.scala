package zzz.akka.investigation.actors.in.the.cloud

import akka.actor.{ Actor, ActorLogging, ActorRef }
import akka.actor.Props

object Plane {
  case object GiveMeControl
}

class Plane extends Actor with ActorLogging {

  import Altimeter._
  import Plane._
  
  val altimeter = context.actorOf(Props[Altimeter])
  val controls = context.actorOf(Props(new ControlSurfaces(altimeter)))
  
  def receive = {
    case GiveMeControl =>
      log.info("Plane gicing control to " + sender)
      sender ! controls // Notice that it's perfectly legal to send a reference
      					// in the response message
  }
}