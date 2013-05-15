package zzz.akka.investigation.actors.in.the.cloud.supervisor

import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers
import org.scalatest.BeforeAndAfterAll
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import akka.actor.Props
import akka.testkit.ImplicitSender
import akka.actor.Terminated
import scala.concurrent.duration._

class DummyIsolatedLifeCycleSupervisor extends IsolatedLifeCycleSupervisor {
  def childStarter() {}
}

@RunWith(classOf[JUnitRunner])
class IsolatedLifeCycleSupervisorSpec extends TestKit(ActorSystem("IsolatedLifecycleSupervisor"))
  with ImplicitSender
  with WordSpec
  with MustMatchers
  with BeforeAndAfterAll {

  import IsolatedLifeCycleSupervisor._

  override def afterAll() {
    system.shutdown()
  }

  "IsolatedLifecycleSupervisor" should {
    "respond with the Started message when interrogated whether it is started" in {
      val supervisor = system.actorOf(Props[DummyIsolatedLifeCycleSupervisor])
      supervisor ! WaitForStart
      expectMsg(Started)
    }
  }
}