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
    "raise an exception when pushing too many messages in the queue" in {
      val boundedMailbox = system.actorOf(Props[BoundedMailboxActor]().withMailbox("bounded-mailbox"))

      boundedMailbox ! Wait(5 seconds)

      for (i <- 1 to 100) {
        boundedMailbox ! "Message " + i
      }
      
      Thread.sleep(7)
    }
  }

}

object BoundedMailboxActor {
  case class Wait(duration: Duration)
}

class BoundedMailboxActor extends Actor with ActorLogging {
  import BoundedMailboxActor._

  def receive = {
    case Wait(duration) =>
      blocking { Thread.sleep(duration.toMillis) }
    case msg => log.info("Received {}", msg)
  }
}

