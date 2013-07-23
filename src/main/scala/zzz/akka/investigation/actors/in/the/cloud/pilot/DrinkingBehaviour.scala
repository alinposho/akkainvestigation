package zzz.akka.investigation.actors.in.the.cloud.pilot

import akka.actor.ActorRef
import akka.actor.Actor

trait DrinkingResolution {
  import scala.util.Random
  import scala.concurrent.duration._

  def initialSobering: FiniteDuration = 1 second
  def soberingInterval: FiniteDuration = 1 second
  def drinkInterval(): FiniteDuration = Random.nextInt(300) seconds
}

object DrikingBehaviour {
  case class BloodAlcoholLevelChanged(level: Float)

  case object FeelingSober
  case object FeelingTipsy
  case object FeelingLikeZaphod

  class DrinkingBehaviourWithResolution(drinker: ActorRef) extends DrinkingBehaviour(drinker) with DrinkingResolution
}

class DrinkingBehaviour(drinker: ActorRef) extends Actor {
  this: DrinkingResolution =>

  import DrikingBehaviour._
  import scala.concurrent.ExecutionContext.Implicits.global

  val SoberingBloodAlcoholLevelDecrease = -0.0001f

  var currentBloodAlcoholLevel = 0f
  def scheduler = context.system.scheduler

  override def postStop(): Unit = sobering.cancel()
  val sobering = scheduler.schedule(initialSobering, soberingInterval, self, BloodAlcoholLevelChanged(SoberingBloodAlcoholLevelDecrease))

  override def preStart(): Unit = drink()
  def drink() = scheduler.scheduleOnce(drinkInterval(), self, BloodAlcoholLevelChanged(0.005f))

  def receive = {
    case BloodAlcoholLevelChanged(amount) =>
      currentBloodAlcoholLevel = (currentBloodAlcoholLevel + amount).max(0f)
      drinker ! drinkAndGetFeeling()
  }

  def drinkAndGetFeeling() = {
    if (currentBloodAlcoholLevel <= 0.01) {
      drink()
      FeelingSober
    } else if (currentBloodAlcoholLevel <= 0.03) {
      drink()
      FeelingTipsy
    } else FeelingLikeZaphod
  }

}