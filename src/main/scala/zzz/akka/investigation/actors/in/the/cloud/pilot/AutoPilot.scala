package zzz.akka.investigation.actors.in.the.cloud.pilot

import akka.actor.Actor
import Pilots.ReadyToGo

class AutoPilot extends Actor {
  
  def receive = {
    case ReadyToGo => 
  }
}