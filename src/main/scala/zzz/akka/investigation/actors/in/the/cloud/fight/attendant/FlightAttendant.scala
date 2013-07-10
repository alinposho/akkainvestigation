package zzz.akka.investigation.actors.in.the.cloud.fight.attendant

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import akka.actor.Cancellable

/**
 * Trait used to customize flight attendants responsiveness time.
 */
trait AttendantResponsiveness {
  val maxResponseTimeMS: Int
  def responseDuration = scala.util.Random.nextInt(maxResponseTimeMS).millis
}

object FlightAttendant {
  case class GetDrink(drinkName: String)
  case class DeliverDrink(drink: Drink)
  case class Drink(name: String)
  case class Assist(passenger: ActorRef)
  case object Busy_?
  case object Yes
  case object No
  
  val MagicHealingPoltion = "Magic Healing Potion"

  class DefaultFlightAttendant extends FlightAttendant with AttendantResponsiveness {
    val FIVE_MINUTES = 300000
    override val maxResponseTimeMS = FIVE_MINUTES
  }

  def apply(): Props = Props[DefaultFlightAttendant]

}

class FlightAttendant extends Actor with ActorLogging { this: AttendantResponsiveness =>

  import FlightAttendant._

  var pendingDelivery: Option[Cancellable] = None

  def scheduleDelivery(drinkName: String): Cancellable = {
    context.system.scheduler.scheduleOnce(responseDuration, self, DeliverDrink(Drink(drinkName)))
  }

  def receive = assistInjuredPassanger orElse handleUnsupportedMsgs

  def assistInjuredPassanger: Receive = {
    case Assist(passanger) =>
      pendingDelivery foreach (_.cancel)
      pendingDelivery = None

      passanger ! Drink(MagicHealingPoltion)
  }

  def handleUnsupportedMsgs: Receive = {
    case msg =>
      throw new Exception(s"Received unexpected message $msg")
  }

  //  def receive = {
  //    case GetDrink(drinkName) =>
  //      sendDrinkWithDelay(drinkName, sender)
  //    case msg =>
  //      log.error(s"Received unexpected message $msg")
  //  }

  private def sendDrinkWithDelay(drinkName: String, receiver: ActorRef): Unit = {
    context.system.scheduler.scheduleOnce(responseDuration, receiver, Drink(drinkName))
  }

}