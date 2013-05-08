package zzz.akka.investigation.pingpong

import akka.testkit.TestKit
import akka.actor.ActorSystem
import akka.testkit.ImplicitSender
import scala.util.Random

class ActorSys(name: String) extends TestKit(ActorSystem(name))
  										with ImplicitSender
  										with DelayedInit {

  def this() = this(s"TestSystem${Random.nextInt(5)}")
  
  override def delayedInit(f: => Unit): Unit = {
    try {
      f
    } finally {
      shutdown()
    }
  }
  
  def shutdown() {
    system.shutdown
  }
  
}