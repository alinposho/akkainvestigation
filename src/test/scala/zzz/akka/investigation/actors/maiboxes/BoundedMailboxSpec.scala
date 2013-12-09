package zzz.akka.investigation.actors.maiboxes

import akka.actor.{ Actor, ActorLogging, ActorRef, ActorSystem, PoisonPill, Props }
import akka.testkit.{ ImplicitSender, TestKit, TestActorRef }
import com.typesafe.config.ConfigFactory
import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import org.scalatest.{ BeforeAndAfterAll, WordSpec }
import org.scalatest.junit.JUnitRunner
import scala.concurrent.duration._
import scala.concurrent._

@RunWith(classOf[JUnitRunner])
class BoundedMailboxSpec extends TestKit(ActorSystem("BoundedMailboxSpec", ConfigFactory.load("boundedMailbox.conf"))) // This configuration will be loaded from the src/test/resources folder
  with WordSpec
  with ImplicitSender
  with MustMatchers
  with BeforeAndAfterAll {

  import BoundedMailboxActor._

  override def afterAll() = system.shutdown()

  val BoundedMailboxSize = 5
  var boundedMailboxActor: ActorRef = _

  "BoundedMailboxActor" should {
    "discard messages sent when the queue is full since the timeout is small" in {
      boundedMailboxActor = system.actorOf(Props[BoundedMailboxActor]().withMailbox("bounded-mailbox-small-timeout")) // in the boundedMailbox.conf file there is a section with this name
      Thread.sleep(1000) // Give the boundedMailboxActor some time to start

      boundedMailboxActor ! Wait(1 seconds)
      sendABurstOfMessages(numberOfMessages = 10)

      expectMsg(3 seconds, DoneWaiting)

      assertPartOfTheMessagesHaveBeenQueuedAndProcessed(expectedNumberOfProcessedMsgs = BoundedMailboxSize)
    }

    def assertPartOfTheMessagesHaveBeenQueuedAndProcessed(expectedNumberOfProcessedMsgs: Int): Unit = {
      boundedMailboxActor ! GetProcessedMessagesCount
      expectMsg(3 seconds, ProcessedMessagesCount(expectedNumberOfProcessedMsgs))
    }

    "not discard messages sent when the queue is full since the timeout will not expire" in {
      boundedMailboxActor = system.actorOf(Props[BoundedMailboxActor]().withMailbox("bounded-mailbox-large-timeout")) // in the boundedMailbox.conf file there is a section with this name
      Thread.sleep(1000) // Give the boundedMailboxActor some time to start

      boundedMailboxActor ! Wait(1 seconds)

      val numberOfMessages = 10
      sendABurstOfMessages(numberOfMessages)

      expectMsg(3 seconds, DoneWaiting)

      assertAllMessagesQueueAndReceived(numberOfMessages)
    }

    def sendABurstOfMessages(numberOfMessages: Int): Unit = {
      for (i <- 1 to numberOfMessages) {
        boundedMailboxActor ! "Message " + i
      }
    }

    def assertAllMessagesQueueAndReceived(expectedNumberOfQueuedMessages: Int) {
      boundedMailboxActor ! GetProcessedMessagesCount
      expectMsg(ProcessedMessagesCount(expectedNumberOfQueuedMessages))
    }
  }

}

object BoundedMailboxActor {
  case class Wait(duration: Duration)
  case object DoneWaiting
  case object GetProcessedMessagesCount
  case class ProcessedMessagesCount(value: Int)
}

class BoundedMailboxActor extends Actor with ActorLogging {
  import BoundedMailboxActor._

  var processedMessages = 0;
  def receive = {
    case Wait(duration) =>
      blocking {
        Thread.sleep(duration.toMillis)
        sender ! DoneWaiting
      }
    case GetProcessedMessagesCount =>
      sender ! ProcessedMessagesCount(processedMessages)
    case msg: String =>
      processedMessages += 1
  }
}

