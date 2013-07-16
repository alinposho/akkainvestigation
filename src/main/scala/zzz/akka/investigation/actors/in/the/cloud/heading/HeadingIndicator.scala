package zzz.akka.investigation.actors.in.the.cloud.heading

import zzz.akka.investigation.actors.in.the.cloud.EventSource
import akka.actor.ActorLogging
import akka.actor.Actor
import scala.concurrent.duration._

object HeadingIndicator {
  case class BankChange(amount: Float)
  case class HeadingUpdate(heading: Float)

  case object Tick

  val MinAmount = 1.0f
  val MaxAmount = -1.0f
}

class HeadingIndicator extends Actor with ActorLogging { this: EventSource =>
  import HeadingIndicator._
  import context._

  val maxDegPerSec = 5
  val ticker = system.scheduler.schedule(100 millis, 100 millis, self, Tick)

  var lastTick = System.currentTimeMillis()
  var rateOfBank = 0f
  var heading = 0f
  
  override def postStop(): Unit = ticker.cancel

  def receive = eventSourceReceive orElse headingIndicatorReceive

  def headingIndicatorReceive: Receive = {
    case BankChange(amount) =>
      rateOfBank = amount.min(MinAmount).max(MaxAmount)
    case Tick =>
      calculateNewHeading()
      sendEvent(HeadingUpdate(heading))
  }

  private def calculateNewHeading(): Unit = {
    val tick = System.currentTimeMillis()
    val timeDelta = (tick - lastTick) / 1000f
    val degrees = rateOfBank * maxDegPerSec
    heading = (heading + (360 + (timeDelta * degrees))) % 360
    lastTick = tick
  }

}