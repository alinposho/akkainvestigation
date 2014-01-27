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
import akka.testkit.TestActor
import akka.testkit.TestActorRef

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
      fsm.setState(stateName = Active, stateData = Todo(system.deadLetters, Vector(1, 2, 3)))

      fsm ! Flush

      assert(fsm.stateName === Idle)
      assert(fsm.stateData === Todo(system.deadLetters, Vector.empty))
    }

    "timeout after 1 second while Active and go to Idle" in {
      // Prepare
      val fsm = TestFSMRef(new Buncher)
      fsm.setState(stateName = Active, stateData = Todo(system.deadLetters, Vector(1, 2, 3)))

      // Exercise
      fsm ! SubscribeTransitionCallBack(testActor)

      // Verify
      expectMsg(CurrentState(fsm, Active))
      expectMsgPF(3.seconds) {
        case Transition(sender, Active, Idle) =>
          assert(sender === fsm)
          assert(fsm.stateName === Idle)
          assert(fsm.stateData === Todo(system.deadLetters, Vector.empty))
      }
    }

    "add the element in the queue when Idle with Todo state data" in {
      runTestThatAddsElementInTheQueueForState(Idle)
    }

    "add the element in the queue when Active with Todo state data" in {
      runTestThatAddsElementInTheQueueForState(Active)
    }

    def runTestThatAddsElementInTheQueueForState(initialState: State): Unit = {
      val fsm = TestFSMRef(new Buncher)
      fsm.setState(stateName = initialState, stateData = Todo(system.deadLetters, Vector.empty))

      val smth = "smth"
      fsm ! Queue(smth)

      assert(fsm.stateName === Active)
      assert(fsm.stateData === Todo(system.deadLetters, Vector(smth)))
    }

    "not batch if uninitialized" in {
      val buncher = TestActorRef[Buncher]
      buncher ! Queue(42)
      expectNoMsg
    }

    "return the batch to the target actor after the timeout has expired" in {
      // Prepare
      val buncher = TestFSMRef(new Buncher)
      buncher ! SetTarget(testActor)

      // Exercise
      buncher ! Queue(42)
      buncher ! Queue(43)

      // Verify
      expectMsg(Batch(Seq(42, 43)))
    }
    
    "return the batch to the target actor after when receiving the Flush message" in {
      // Prepare
      val buncher = TestFSMRef(new Buncher)
      buncher ! SetTarget(testActor)

      // Exercise
      buncher ! Queue(42)
      buncher ! Flush
      buncher ! Queue(43)

      // Verify
      expectMsg(Batch(Seq(42))) // Message as a result of the Flush event
      expectMsg(Batch(Seq(43))) // Message as a result of the StateTimeout event 
    }

  }

}