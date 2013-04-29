package zzz.akka.investigation.actors.in.the.cloud

import akka.actor.{ Actor, ActorLogging, ActorSystem, Props }
// The duration package  objects extends Ints with some timing functionality
import scala.concurrent.duration._
// The scheduler needs and excution context - we'll just use the global one
import scala.concurrent.ExecutionContext.Implicits.global

object Altimeter {
  val MinAmount = -1.0F
  val MaxAmount = 1.0F

  // This message is sent to the Altimeter to inform about the rate of climb
  case class RateChange(amount: Float)
  case class AltitudeUpdate(altitude: Double)
}

class Altimeter extends Actor with ActorLogging with EventSource {

  import Altimeter._

  val maxCeilingInFeet = 43000
  // In "feet per minute"
  val maxRateOfClimb = 5000
  var rateOfClimb: Float = 0
  var altitude = 0.0 // Initialized to Double
  // As time passes, we need to change the altitude based on the time passed.
  // The lastTick allows us to figure out how much time has passed
  var lastTick = System.currentTimeMillis()

  val ticker = context.system.scheduler.schedule(100.millis, 100.millis, self, Tick)
  case object Tick

  override def receive = eventSourceReceive orElse altimeterReceive

  def altimeterReceive: Receive = {
    case RateChange(amount) =>
      rateOfClimb = keepTheValueWithin(MinAmount, MaxAmount, amount) * maxRateOfClimb
      log.info(s"Altimeter changed rate of climb to $rateOfClimb.")

    // Calculate a new altitude
    case Tick =>
      calculateNewAltitude()
      sendEvent(AltitudeUpdate(altitude))
    // Not needed any longer since the Plane is logging the same info
    // when listening for the AltitudeUpdate event.
    //      log.info(s"Changed current altitude to $altitude") 
    case msg =>
      throw new RuntimeException("Unknown message received: " + msg);

  }

  private def keepTheValueWithin(min: Float, max: Float, amount: Float) = {
    amount.min(max).max(min)
  }

  private def calculateNewAltitude(): Unit = {
    val tick = System.currentTimeMillis
    altitude = altitude + ((tick - lastTick) / 60000.0) * rateOfClimb
    lastTick = tick
  }

  // Kill our ticker when we stop
  override def postStop(): Unit = ticker.cancel

}

