package zzz.akka.investigation.actors.in.the.cloud.altimeter
import akka.actor.{Actor, ActorLogging, Props}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import zzz.akka.investigation.actors.in.the.cloud.EventSource
import zzz.akka.investigation.actors.in.the.cloud.ProductionEventSource

object Altimeter {
  val MinAmount = -1.0F
  val MaxAmount = 1.0F
  
  val Name = "Altimeter"

  // This message is sent to the Altimeter to inform about the rate of climb
  case class RateChange(amount: Float)
  case class AltitudeUpdate(altitude: Double)
  
  class AltemeterWithEventSource extends Altimeter with ProductionEventSource
  def apply(): Props = Props[AltemeterWithEventSource]
  
}

class Altimeter extends Actor with ActorLogging { this: EventSource =>

  import Altimeter._

  val maxCeilingInFeet = 43000
  // In "feet per minute"
  val maxRateOfClimb = 5000
  var rateOfClimb: Float = 0
  var altitude = 0.0
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
    case Tick =>
      calculateNewAltitude()
      sendEvent(AltitudeUpdate(altitude))
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

