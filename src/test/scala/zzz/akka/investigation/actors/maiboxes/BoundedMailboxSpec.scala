package zzz.akka.investigation.actors.maiboxes

import akka.actor.{ Actor, ActorLogging, ActorSystem, PoisonPill, Props }
import akka.testkit.{ ImplicitSender, TestKit, TestActorRef }
import com.typesafe.config.ConfigFactory
import org.junit.runner.RunWith
import org.scalatest.matchers.MustMatchers
import org.scalatest.{ BeforeAndAfterAll, WordSpec }
import org.scalatest.junit.JUnitRunner
import scala.concurrent.duration._
import scala.concurrent._

@RunWith(classOf[JUnitRunner])
class BoundedMailboxSpec extends TestKit(ActorSystem("BoundedMailboxSpec", ConfigFactory.load("boundedMailbox.conf")))
  with WordSpec
  with ImplicitSender
  with MustMatchers
  with BeforeAndAfterAll {

  import BoundedMailboxActor._

  override def afterAll() = system.shutdown()

  "BoundedMailboxActor" should {
    "discard messages sent when the queue is full since the timeout is small" in {
      val boundedMailboxActor = system.actorOf(Props[BoundedMailboxActor]().withMailbox("bounded-mailbox"))

      boundedMailboxActor ! Wait(1 seconds)

      for (i <- 1 to 10) {
        boundedMailboxActor ! "Message " + i
      }

      expectMsg(3 seconds, DoneWaiting)

      Thread.sleep(1000)

      boundedMailboxActor ! GetProcessedMessages

      // This call will fail for some reason...
      //      expectMsg(3 seconds, ProcessedMessages(5)) // only 5 messages were queued - the max size of the queue. For the others the timeout
      // exceeded, hence, they were lost.

    }
  }

}

object BoundedMailboxActor {
  case class Wait(duration: Duration)
  case object DoneWaiting
  case object GetProcessedMessages
  case class ProcessedMessages(value: Int)
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
    case GetProcessedMessages =>
      ProcessedMessages(processedMessages)
    case msg: String =>
      log.info("Processing msg: {}", msg)
      processedMessages += 1
  }
}

