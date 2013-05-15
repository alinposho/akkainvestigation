package zzz.akka.investigation.actors.in.the.cloud.supervisor

import scala.concurrent.duration.Duration
import akka.actor.ActorInitializationException
import akka.actor.SupervisorStrategy._
import akka.actor.ActorKilledException

abstract class IsolatedResumeSupervisor(maxNrRetries: Int = SupervisionStrategyFactory.InfinteNumberOfRetries,
  withinTimeRange: Duration = Duration.Inf)
  extends IsolatedLifeCycleSupervisor {
  this: SupervisionStrategyFactory =>

  override val supervisorStrategy = makeStrategy(maxNrRetries, withinTimeRange) {
    case _: ActorInitializationException => Stop
    case _: ActorKilledException => Stop
    case _: Exception => Resume
    case _ => Escalate
  }

}