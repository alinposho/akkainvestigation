package zzz.akka.investigation.pingpong

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props

object MyActor {
  case class Ping
  case class Pong
  
  def apply(name: String): Props = Props(classOf[MyActor], name)  
}

class MyActor(name: String) extends Actor with ActorLogging {

  assert(name == "MyActor")

  import MyActor._

  override def receive = {
    case Ping => sender ! Pong
    case Pong => sender ! Ping
  }
}