package zzz.akka.investigation.test.probe

import akka.actor.Actor
import akka.actor.ActorRef

class AnnoyingActor(snooper: ActorRef) extends Actor {
  
  override def preStart() = {
    self ! 'send
  }
  
  def receive = {
    case 'send =>
      snooper ! "Hello!!!"
      self ! 'send
  }

}

class NiceActor(snooper: ActorRef) extends Actor {
  def receive = {
    case 'send => 
      snooper ! "Hi!"
  } 
}