package fsm

import akka.testkit.TestKit
import akka.testkit.ImplicitSender
import org.scalatest.matchers.MustMatchers
import org.scalatest.BeforeAndAfterAll
import org.junit.runner.RunWith
import org.scalatest.WordSpec
import akka.actor._
import org.scalatest.junit.JUnitRunner
import akka.testkit.TestFSMRef
import akka.actor.FSM._
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.Await

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

    "change the Idle state data to Todo after receiving the SetTarget message" in {
      val fsm = TestFSMRef(new Buncher)

      fsm ! SetTarget(testActor)

      assert(fsm.stateName === Idle)
      assert(fsm.stateData === Todo(testActor, Vector.empty))
    }

    "go to Idle after receiving the Flush message when Active with Todo state data" in {
      val fsm = TestFSMRef(new Buncher)
      fsm.setState(stateName = Active, stateData = Todo(testActor, Vector(1, 2, 3)))

      fsm ! Flush

      assert(fsm.stateName === Idle)
      assert(fsm.stateData === Todo(testActor, Vector.empty))
    }

    "timeout after 1 second while Active and go to Idle" in {
      // Prepare
      val fsm = TestFSMRef(new Buncher)
      fsm.setState(stateName = Active, stateData = Todo(testActor, Vector(1, 2, 3)))

      // Exercise
      fsm ! SubscribeTransitionCallBack(testActor)

      // Verify
      expectMsg(CurrentState(fsm, Active))
      expectMsgPF(3 seconds) {
        case Transition(sender, Active, Idle) =>
          assert(sender === fsm)
          assert(fsm.stateName === Idle)
          assert(fsm.stateData === Todo(testActor, Vector.empty))
      }
    }
  }

}