package zzz.akka.agents.oficial.doc

import akka.agent._

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class AgentsAreMonadicTest extends FunSuite {

  test("Agents can be composed using foreach") {
    val agents = create(numberOfAgents = 10)

    var result = 0
    for (agent <- agents; value <- agent) {
      result = result + value
    }

    assert(result === 55)
  }

  test("Agents can be composed using for comprehension") {
    val agent = Agent(5)

    val result = for (value <- agent) yield value + 1

    assert(result.get === 6)
  }
  
  test("Agents can be composed using flatMap as for comprehension") {
    val agent1, agent2 = Agent(5)

    val result = for {
      value1 <- agent1
      value2 <- agent2
    } yield value1 + value2

    assert(result.get === 10)
  }

  
  test("Agents can be composed using flatMap") {
    val agent1, agent2 = Agent(5)

    val result = agent1 flatMap (v => agent2 map (_ + v))

    assert(result.get === 10)
  }


  private def create(numberOfAgents: Int): IndexedSeq[Agent[Int]] = {
    for (i <- 1 to numberOfAgents) yield Agent(i)
  }

}