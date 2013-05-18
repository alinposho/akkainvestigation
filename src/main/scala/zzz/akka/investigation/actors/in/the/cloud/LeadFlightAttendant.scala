package zzz.akka.investigation.actors.in.the.cloud

import akka.actor.Props
import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.ActorRef
import scala.util.Random

trait AttendantCreationPolicy {
  val numberOfAttendants = 8
  def createAttendant: Props = FlightAttendant()
}

/**
 * A trait that adds flexibility on FlightAttendant creation.
 */
trait LeadFlightAttendantProvider {
  def newLeadFlightAttendant: Props = LeadFlightAttendant()
}

object LeadFlightAttendant {

  case object GetFlightAttendant
  case class Attendant(a: ActorRef)

  class DefaultLeadFlightAttendant extends LeadFlightAttendant with AttendantCreationPolicy
  def apply(): Props = Props[DefaultLeadFlightAttendant]
}

class LeadFlightAttendant extends Actor with ActorLogging { this: AttendantCreationPolicy =>

  import LeadFlightAttendant._

  override def preStart() {
    createSubordinates
  }

  private def createSubordinates: Unit = {
    import scala.collection.JavaConverters._

    val attendantNames = context.system.settings.config.getStringList(
      "zzz.akka.avionics.flightcrew.attendantNames").asScala
    attendantNames take numberOfAttendants foreach (createAttendant)
  }

  private def createAttendant(name: String): Unit = {
    context.actorOf(createAttendant, name)
  }

  def takeRandomAttendant(): ActorRef = {
    context.children.take(Random.nextInt(numberOfAttendants) + 1).last
  }

  def receive = {
    case GetFlightAttendant =>
      sender ! Attendant(takeRandomAttendant())
    case m =>
      takeRandomAttendant() forward m
  }

}

