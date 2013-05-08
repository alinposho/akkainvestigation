package zzz.akka.investigation.actors.in.the.cloud

import java.util.concurrent.CountDownLatch
import akka.actor.ActorSystem
import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import org.scalatest.BeforeAndAfterAll
import akka.actor.Actor
import akka.testkit.TestActorRef
import akka.actor.Props


object EventSourceSpy {
  val latch = new CountDownLatch(1)
}

trait EventSourceSpy extends EventSource {
  override def sendEvent[T](event: T): Unit = EventSourceSpy.latch.countDown()
  override def eventSourceReceive = {
    // Make sure we don't interfere with the other events by matching only the empty string event
    case "" => println("Received empty string");
  }
}

class AltimeterSpec extends TestKit(ActorSystem("AltemeterSpec")) 
						with ImplicitSender
						with WordSpec
						with MustMatchers 
						with BeforeAndAfterAll {
  
  import Altimeter._
  
  val MaxRateOfClimbChange = 1f
  
  override def afterAll(): Unit = system.shutdown()
  
  class SlicedAltimenter extends Altimeter with EventSourceSpy
  
  def actor() = {
    TestActorRef[Altimeter](Props[SlicedAltimenter])
  }   
  
  "Altimeter" should {
    
	  "record rate of climb changes" in {
	    val real = actor().underlyingActor
	    
	    real.receive(RateChange(MaxRateOfClimbChange))
	    
	    real.rateOfClimb must be (real.maxRateOfClimb)
	  }
    
    
  }
  
}

