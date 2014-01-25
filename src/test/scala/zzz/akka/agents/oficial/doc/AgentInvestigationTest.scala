package zzz.akka.agents.oficial.doc

import akka.actor._
import akka.agent._
import akka.testkit.{ ImplicitSender, TestKit, TestActorRef }
import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import org.scalatest.{ BeforeAndAfterAll, WordSpec }
import org.scalatest.junit.JUnitRunner

import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class AgentInvestigationTest extends TestKit(ActorSystem("AgentInvestigationTest"))
  with WordSpec
  with ImplicitSender
  with MustMatchers
  with BeforeAndAfterAll {

  override def afterAll() = system.shutdown()

  implicit val exec = system.dispatcher

  "Agent" should {
    "require an ExecutionContext when created" in {
      val agent = Agent(5)(system.dispatcher)

      assert(agent.get === 5, "Should receive the agent's initial value")
    }

    "not require an ExecutionContext if an implicit one is defined in scope" in {
      implicit val exec = system.dispatcher
      val agent = Agent(5)

      assert(agent.get === 5, "Should receive the agent's initial value")
    }

    "updated its value when calling send" in {
      val agent = Agent(5)

      agent send 9 // send does not update the agent immediately

      assert(agent.get != 9)
      assert(Await.result(agent.future, 3 seconds) === 9) // But one can always use a future and block for all the updates to complete
    }

    "this is an interesting sequence of events" in {
      val agent = Agent(5)
      val firstUpdate = 9

      agent send firstUpdate
      val future = agent.future
      agent send 19

      assert(Await.result(future, 3 seconds) === firstUpdate) // Did we get the last update or not?
    }

    "accept functions that modify their internal behaviour" in {
      val agent = Agent(5)

      agent send (x =>
        //do something interesting with the Agents current value
        x + 1)

      assert(Await.result(agent.future, 3 seconds) === 6) // The result of executing the function
    }

    "execute operations sent using sendOff in another execution context" in {
      val agent = Agent("Blah")

      agent send "1234"

      agent sendOff { x =>
        x toCharArray () mkString ("/")
      }

      assert(Await.result(agent.future, 3 seconds) === "1/2/3/4") // The result of executing the function
    }
  }

}