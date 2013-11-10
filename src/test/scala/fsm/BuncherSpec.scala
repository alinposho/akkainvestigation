package fsm

import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import org.scalatest.matchers.MustMatchers
import org.scalatest.BeforeAndAfterAll
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import akka.actor.ActorSystem
import org.scalatest.junit.JUnitRunner
import akka.testkit.TestFSMRef

@RunWith(classOf[JUnitRunner])
class BuncherSpec extends TestKit(ActorSystem("Buncher"))
								with WordSpec
								with ImplicitSender
								with MustMatchers
								with BeforeAndAfterAll {

  import fsm.buncher.States._
  import fsm.buncher.Events._
  
  override def afterAll() = system.shutdown()
  
  "Buncher" should {
    "start in the Idle state with Uninitialized data" in {
      val fsm = TestFSMRef(new Buncher)
      
      assert(fsm.stateName === Idle)
      assert(fsm.stateData === Uninitialized) 
    }
    
    "go to Todo state date when idle after receiving the SetTarget message" in {
      val fsm = TestFSMRef(new Buncher)
      
      fsm ! SetTarget(testActor)
      
      assert(fsm.stateName === Idle)
      assert(fsm.stateData === Todo(testActor, Vector.empty) )
    }
  }


}