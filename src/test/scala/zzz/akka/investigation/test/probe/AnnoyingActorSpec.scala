package zzz.akka.investigation.test.probe

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import org.scalatest.BeforeAndAfterAll
import akka.actor.Props
import org.junit.Ignore
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AnnoyingActorSpec extends TestKit(ActorSystem("AnnoyingActor"))
							with WordSpec 
							with MustMatchers 
							with BeforeAndAfterAll {
  
  override def afterAll() = system.shutdown()
  
  "The AnnoyingActor" should {
    "say Hello!!!" in {
      val annoying = system.actorOf(Props(classOf[AnnoyingActor], testActor))
      expectMsg("Hello!!!")
      system.stop(annoying)
    }
  }
  
  // This test is expected to fail from time to time since the AnnoyingActor
  // from the previous test is not going to stop instantaneously, consequently
  // it will continue to send "Hello!!!" messages to the testActor even during this
  // test
//  "The NiceActor" should {
//    "say Hi!" in {
//      val nice = system.actorOf(Props(classOf[NiceActor], testActor))
//      
//      nice ! 'send
//      
//      expectMsg("Hi!")
//      system.stop(nice)
//    }
//  }

}