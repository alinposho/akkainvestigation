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

@RunWith(classOf[JUnitRunner])
class DrinkingBehaviourSpec extends TestKit(ActorSystem("AutoPilotSpec"))
							  with MustMatchers
							  with ImplicitSender
							  with WordSpec
							  with BeforeAndAfterAll {
  import DrinkingBehaviour._
  
  override def afterAll(): Unit = system.shutdown()
  
  "DrinkingBehaviour actor" should {
    "start drinking right after being created" in {
    	val behaviour = TestActorRef[TestDrinkingBehaviour](Props(classOf[TestDrinkingBehaviour], testActor))
    			
    	expectMsg(FeelingSober)
    	assert(behaviour.underlyingActor.currentBloodAlcoholLevel > 0)
    }
    
    "go from FeelingSober to FeelingTipsy" in {
    	val behaviour = TestActorRef[TestDrinkingBehaviour](Props(classOf[TestDrinkingBehaviour], testActor))
    			
    	expectMsg(FeelingSober)

    	fishForMessage(20 seconds, "We did not reach the tipsy state in 5 seconds"){
    	  case FeelingTipsy =>
    	  	behaviour.underlyingActor.currentBloodAlcoholLevel > SoberBloodAlcoholLimit
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