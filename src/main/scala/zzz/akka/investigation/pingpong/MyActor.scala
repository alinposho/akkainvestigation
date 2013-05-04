package zzz.akka.investigation.pingpong

import akka.actor.Actor
import akka.actor.ActorLogging

object MyActor {
  case class Ping
  case class Pong
}


class MyActor extends Actor with ActorLogging{
  import MyActor._
  
	override def receive = {
	  case Ping => sender ! Pong
	  case pong => sender ! Ping
	}
}