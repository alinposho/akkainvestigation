package zzz.akka.agents.oficial.doc

import akka.actor._
import akka.agent.Agent
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps


object CreatingAgents {
  
  def main(args: Array[String]): Unit = {
    
    val agent = Agent(5)
    println("Agent value is: " + agent.get)
    
    val execContext = global
    val agentWithExplicitExecContext = Agent("Explicit execution context")(execContext)
    println("Agent with explicit execution context value: " + agentWithExplicitExecContext.get)
    
  }

}