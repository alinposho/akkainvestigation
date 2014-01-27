package zzz.akka.investigation.actors.in.the.cloud.bathroom

import akka.actor._
import akka.agent._
import akka.testkit._
import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import org.scalatest.{ BeforeAndAfterAll, WordSpec }
import org.scalatest.junit.JUnitRunner

import scala.collection.immutable.Queue
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

@RunWith(classOf[JUnitRunner])
class BathroomSpec extends TestKit(ActorSystem("BathroomFSMTest"))
  with WordSpec
  with ImplicitSender
  with MustMatchers
  with BeforeAndAfterAll {

  import Bathroom._

  override def afterAll() = system.shutdown()

  implicit val exec = system.dispatcher

  "Bathroom" should {
    "start in the Vacant state with NotInUse data" in {
      val bathroom = TestFSMRef(new Bathroom(createAgent(Male), createAgent(Female)))

      assert(bathroom.stateName === Vacant)
      assert(bathroom.stateData === NotInUse)
    }

    "go to the Occupied state after receiving the IWannaUseTheBathroom event" in {
      val bathroom = TestFSMRef(new Bathroom(createAgent(Male), createAgent(Female)))

      bathroom ! IWannaUseTheBathroom

      assert(bathroom.stateName === Occupied)
      assertStateDataIs(by = testActor, queue = Queue(), state = bathroom.stateData)
    }

    "respond with YouCanUseTheBathroomNow when receiving IWannaUseThebathroom event in state Vacant" in {
      val bathroom = TestFSMRef(new Bathroom(createAgent(Male), createAgent(Female)))

      bathroom ! IWannaUseTheBathroom

      expectMsg(YouCanUseTheBathroomNow)
    }

    "ignore a Finished event that does not come from the occupant" in {
      val bathroom = TestFSMRef(new Bathroom(createAgent(Male), createAgent(Female)))
      bathroom ! IWannaUseTheBathroom
      val probe = TestProbe()

      probe.send(bathroom, Finished(Male)) // This message is ignored 

      assertStateDataIs(by = testActor, queue = Queue(), state = bathroom.stateData)
    }

    "send stats to the male and female agents" in {
      val maleStatsAgent = createAgent(Male)
      val femaleStatsAgent = createAgent(Female)
      val bathroom = TestFSMRef(new Bathroom(femaleStatsAgent, maleStatsAgent))

      occupyThe(bathroom, occupantGender = Male, times = 5)
      occupyThe(bathroom, occupantGender = Female, times = 4)

      assertStatsAre(gender = Male, count = 5, agent = maleStatsAgent)
      assertStatsAre(gender = Female, count = 4, agent = femaleStatsAgent)
    }

  }

  private def createAgent(gender: Gender) = Agent(GenderAndTime(gender, 0.seconds, 0))

  private def assertStateDataIs(by: ActorRef, queue: Queue[ActorRef], state: Data): Unit = {
    state match {
      case InUse(by, atTime, stateQueue) => {
        assert(by === testActor)
        assert(queue === stateQueue, s"The queue should be queue")
      }
      case NotInUse => throw new AssertionError("The state data should be InUse")
    }
  }

  private def occupyThe(bathroom: TestFSMRef[State, Data, Bathroom], occupantGender: Gender, times: Int): Unit = {
    val probes = for (i <- 1 to times) yield TestProbe()

    for (probe <- probes) {
      probe send (bathroom, IWannaUseTheBathroom)
    }

    for (probe <- probes) {
      probe send (bathroom, Finished(occupantGender))
    }
  }

  private def assertStatsAre(gender: Gender, count: Int, agent: Agent[GenderAndTime]): Unit = {
    Await.result(agent.future, 3 seconds) match {
      case GenderAndTime(agentGender, peakDuration, numberOfToiletUses) => {
    	  assert(agentGender === gender)
    	  assert(count === numberOfToiletUses)
    	  assert(peakDuration > 0.seconds)
      }
    }
  }
}