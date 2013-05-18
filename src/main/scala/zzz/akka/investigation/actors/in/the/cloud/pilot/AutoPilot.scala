package zzz.akka.investigation.actors.in.the.cloud.pilot

import akka.actor.Actor
import Pilots.ReadyToGo

object AutoPilot {
  val Name = "Autopilot"
}

class AutoPilot extends Actor {
  
  def receive = {
    case ReadyToGo => 
      throw new Exception(s"This actor ${self.path.name} should not receive the ReadyToGo message!")
  }
}