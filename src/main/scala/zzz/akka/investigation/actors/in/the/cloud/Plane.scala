package zzz.akka.investigation.actors.in.the.cloud

import akka.actor.{ Actor, ActorLogging, ActorRef }
import akka.actor.Props

object Plane {
  case object GiveMeControl
}

class Plane extends Actor with ActorLogging {

  import Altimeter._
  import Plane._
  import EventSource.RegisterListener

  // Need to call the companion object constructor for Altimeter otherwise, an exception will be raised since
  // the class doesn't define the "eventSourceReceive" method 
  val altimeter = context.actorOf(Altimeter())
  val controls = context.actorOf(Props(classOf[ControlSurfaces], altimeter))

  // This is deprecated. Use the code above
  // val controls = context.actorOf(Props(new ControlSurfaces(altimeter))) 

  override def preStart() {
    altimeter ! RegisterListener(self)
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