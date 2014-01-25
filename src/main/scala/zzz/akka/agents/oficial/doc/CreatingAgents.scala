package zzz.akka.agents.oficial.doc

import akka.actor._
import akka.agent.Agent
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps



object CreatingAgents {
  
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("CreatingAgents")
    
    val agent = Agent(5)
    
    println("Agent value is: " + agent.get)
    
    system.scheduler.scheduleOnce(5 seconds){
      system.shutdown()
    }
  }

}