package zzz.akka.investigation.test.probe

import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.testkit.TestKit
import akka.testkit.TestProbe
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TestProbeSpec extends TestKit(ActorSystem("TestProbeActor"))
							with WordSpec 
							with MustMatchers 
							with BeforeAndAfterAll {
  
  override def afterAll() = system.shutdown()
  
  "The AnnoyingActor" should {
    "say Hello!!!" in {
      val testProbe = TestProbe()
      
      val annoying = system.actorOf(Props(classOf[AnnoyingActor], testProbe.ref))
      
      testProbe.expectMsg("Hello!!!")
      system.stop(annoying)
    }
  }

  // The test will no longer fail since the AnnoyingActor is interacting with
  // its own TestProbe instance
  "The NiceActor" should {
    "say Hi!" in {
      val nice = system.actorOf(Props(classOf[NiceActor], testActor))
      
      nice ! 'send
      
      expectMsg("Hi!")
      system.stop(nice)
    }
  }

}