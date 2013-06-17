package zzz.akka.investigation.actors.in.the.cloud.fight.attendant

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props

/**
 * Trait used to customize flight attendants responsiveness time.
 */
trait AttendantResponsiveness {
  val maxResponseTimeMS: Int
  def responseDuration = scala.util.Random.nextInt(maxResponseTimeMS).millis
}

object FlightAttendant {
  case class GetDrink(drinkName: String)
  case class Drink(name: String)

  class DefaultFlightAttendant extends FlightAttendant with AttendantResponsiveness {
    val FIVE_MINUTES = 300000
    override val maxResponseTimeMS = FIVE_MINUTES
  }

  def apply(): Props = Props[DefaultFlightAttendant]

}

class FlightAttendant extends Actor with ActorLogging { this: AttendantResponsiveness =>

  import FlightAttendant._

  def receive = {
    case GetDrink(drinkName) =>
      sendDrinkWithDelay(drinkName, sender)
    case msg =>
      log.error(s"Received unexpected message $msg")
  }

  private def sendDrinkWithDelay(drinkName: String, receiver: ActorRef): Unit = {
    context.system.scheduler.scheduleOnce(responseDuration, sender,
      Drink(drinkName))
  }

}