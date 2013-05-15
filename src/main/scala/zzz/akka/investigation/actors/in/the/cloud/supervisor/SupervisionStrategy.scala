package zzz.akka.investigation.actors.in.the.cloud.supervisor

import scala.concurrent.duration.Duration
import akka.actor.SupervisorStrategy
import SupervisorStrategy.Decider
import akka.actor.OneForOneStrategy
import akka.actor.AllForOneStrategy
import akka.actor.AllForOneStrategy

object SupervisionStrategyFactory {
  val InfinteNumberOfRetries = -1;
}

trait SupervisionStrategyFactory {
  def makeStrategy(maxNrRetries: Int, withinTimeRange: Duration)(decider: Decider): SupervisorStrategy
}

// Replace the following two traits with just one, providing the class to be instantiated as a parameter in the 
// constructor
trait OneForOneSupervisionStrategy extends SupervisionStrategyFactory {
  def makeStrategy(maxNrRetries: Int, withinTimeRange: Duration)(decider: Decider) = {
    OneForOneStrategy(maxNrRetries, withinTimeRange)(decider)
  }
}

trait AllForOneSupervisionStrategy extends SupervisionStrategyFactory {
  def makeStrategy(maxNrRetries: Int, withinTimeRange: Duration)(decider: Decider) = {
    AllForOneStrategy(maxNrRetries, withinTimeRange)(decider)
  }
}

