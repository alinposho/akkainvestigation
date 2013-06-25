package zzz.akka.investigation

import akka.actor.Actor

case class Hello(greeting: String)
case class GoodBye(greeting: String)

class ChangingBehaviourActor extends Actor {
  
  def expectHello: Receive = {
    case Hello(greeting) =>
      sender ! Hello(greeting + " to you too!")
      context.become(expectGoodbye)
    case GoodBye(_) => 
      sender ! "Huh! Who are you?"
  }
  
  def expectGoodbye: Receive = {
    case Hello(_) =>
      sender ! "We've already done that?"
      
    case GoodBye(_) => 
      sender ! GoodBye("So long, dood!")
      context.become(expectHello)
  }
  
  def receive = expectHello
}