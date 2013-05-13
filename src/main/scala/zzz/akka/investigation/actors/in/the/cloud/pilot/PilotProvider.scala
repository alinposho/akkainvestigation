package zzz.akka.investigation.actors.in.the.cloud.pilot

import akka.actor.Actor

trait PilotProvider {
  def pilot: Actor = new Pilot
  def copilot: Actor = new CoPilot
}
