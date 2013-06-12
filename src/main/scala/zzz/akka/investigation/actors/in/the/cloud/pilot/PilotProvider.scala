package zzz.akka.investigation.actors.in.the.cloud.pilot

import akka.actor.Actor
import akka.actor.Props

trait PilotProvider {
  def newPilot = Props[Pilot]
  def newCoPilot= Props[CoPilot]
  def newAutopilot = Props[AutoPilot]
}
