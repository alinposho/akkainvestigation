package zzz.akka.investigation.actors.in.the.cloud.pilot

import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import org.scalatest.matchers.MustMatchers
import org.scalatest.BeforeAndAfterAll
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import akka.actor.ActorSystem
import org.scalatest.junit.JUnitRunner
import akka.actor.ActorRef
import akka.testkit.TestActorRef
import akka.actor.Props
import scala.concurrent.duration._

// TODO: Make the test create a new instanc of the TestDrinkingBehaviour actor for each test!!!!
@RunWith(classOf[JUnitRunner])
class DrinkingBehaviourSpec extends TestKit(ActorSystem("DrinkingBehaviourSpec"))
							  with MustMatchers
							  with ImplicitSender
							  with WordSpec
							  with BeforeAndAfterAll {
  import DrinkingBehaviour._

  def createDrikingBehaviourActor() = TestActorRef[TestDrinkingBehaviour](Props(classOf[TestDrinkingBehaviour], testActor))
  override def afterAll(): Unit = system.shutdown()

  "DrinkingBehaviour actor" should {
    "start drinking right after being created" in {
      val behaviour = createDrikingBehaviourActor

      expectMsg(FeelingSober)
    }

    "maintain blood alcohol level zero or positive while FeelingSober" in {
      val behaviour = createDrikingBehaviourActor

      expectMsg(FeelingSober)
      assert(behaviour.underlyingActor.currentBloodAlcoholLevel >= 0.0f)
    }

    "maintain blood alcohol level bellow limit while FeelingSober" in {
      val behaviour = createDrikingBehaviourActor

      expectMsg(FeelingSober)
      assert(behaviour.underlyingActor.currentBloodAlcoholLevel <= SoberBloodAlcoholLimit)
    }

    "go from FeelingSober to FeelingTipsy" in {
      val behaviour = createDrikingBehaviourActor
      expectMsg(FeelingSober)

      fishForMessage(5 seconds, "Waiting to get tipsy") {
        case FeelingTipsy =>
          behaviour.underlyingActor.currentBloodAlcoholLevel > SoberBloodAlcoholLimit
        case _ => false
      }
    }

    "go from FeelingTipsy to FeelingLikeZaphod" in {
      val behaviour = createDrikingBehaviourActor

      fishForMessage(5 seconds, "Waiting to get tipsy") {
        case FeelingLikeZaphod =>
          behaviour.underlyingActor.currentBloodAlcoholLevel > TipsyBloodAlcoholLimit
        case _ => false
      }
    }
    
  }
}

class TestDrinkingBehaviour(drinker: ActorRef) extends DrinkingBehaviour(drinker) with TestDrinkingResolution

trait TestDrinkingResolution extends DrinkingResolution {
  import scala.util.Random
  import scala.concurrent.duration._

  override def initialSobering: FiniteDuration = 1 nano
  override def soberingInterval: FiniteDuration = 1 nano
  override def drinkInterval(): FiniteDuration = Random.nextInt(300) nanos
}