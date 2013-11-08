package zzz.akka.investigation.actors.in.the.cloud.pilot

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.duration.FiniteDuration
import scala.util.Random
import DrinkingBehaviour.AlcoholBloodLevelIncreasePerDrinkInterval
import DrinkingBehaviour.SoberBloodAlcoholLimit
import DrinkingBehaviour.SoberingBloodAlcoholLevelDecrease
import DrinkingBehaviour.TipsyBloodAlcoholLimit
import akka.actor.Actor
import akka.actor.ActorRef
import akka.actor.actorRef2Scala
import akka.actor.Props

trait DrinkingProvider {
  import DrinkingBehaviour._
  def newDrinkingBehaviour(drinker: ActorRef) = Props(classOf[DrinkingBehaviourWithResolution], drinker)
}

trait DrinkingResolution {
  import scala.util.Random
  import scala.concurrent.duration._

  def initialSobering: FiniteDuration = 1 second
  def soberingInterval: FiniteDuration = 1 second
  def drinkInterval(): FiniteDuration = Random.nextInt(300) seconds
}

object DrinkingBehaviour {
  case class BloodAlcoholLevelChanged(level: Float)

  case object FeelingSober
  case object FeelingTipsy
  case object FeelingLikeZaphod

  class DrinkingBehaviourWithResolution(drinker: ActorRef) extends DrinkingBehaviour(drinker) with DrinkingResolution

  val SoberBloodAlcoholLimit = 0.01
  val TipsyBloodAlcoholLimit = 0.03
  val SoberingBloodAlcoholLevelDecrease = -0.0001f
  val AlcoholBloodLevelIncreasePerDrinkInterval = 0.005f
}

class DrinkingBehaviour(drinker: ActorRef) extends Actor {
  this: DrinkingResolution =>

  import DrinkingBehaviour._
  import scala.concurrent.ExecutionContext.Implicits.global

  var currentBloodAlcoholLevel = 0f
  def scheduler = context.system.scheduler

  override def preStart(): Unit = drink()
  private def drink() = scheduler.scheduleOnce(drinkInterval(), self, BloodAlcoholLevelChanged(AlcoholBloodLevelIncreasePerDrinkInterval))

  override def postStop(): Unit = sobering.cancel()
  val sobering = scheduler.schedule(initialSobering, soberingInterval, self, BloodAlcoholLevelChanged(SoberingBloodAlcoholLevelDecrease))

  def receive = {
    case BloodAlcoholLevelChanged(amount) =>
      currentBloodAlcoholLevel = (currentBloodAlcoholLevel + amount).max(0f)
      drinker ! getStateAfterDrinking()
  }

  def getStateAfterDrinking() = {
    if (currentBloodAlcoholLevel <= SoberBloodAlcoholLimit) {
      drink()
      FeelingSober
    } else if (currentBloodAlcoholLevel <= TipsyBloodAlcoholLimit) {
      drink()
      FeelingTipsy
    } else FeelingLikeZaphod
  }

}