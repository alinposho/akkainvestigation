package zzz.akka.actor.routing

import com.typesafe.config.ConfigFactory

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.actorRef2Scala
import akka.routing.FromConfig
import scala.concurrent.duration._

object RoundRobinRoutingTest {

  def main(args: Array[String]) {
    val system = ActorSystem("RoundRobinActosSystemTest")
    // In order for this to work, the configuration file "application.conf" has to be in the classpath and
    // it has to contain the configuration for the "DatabaseConnectionRouter" under "akka { actor { configuration {...}}}"
    // otherwise it won't work. On further inspection one might find it possible to customize that.
    val dbRouter = system.actorOf(Props[DBConnection].withRouter(FromConfig()), "DatabaseConnectionRouter")

    for (no <- 1 to 40) {
      dbRouter ! "GetMeNewConnection"
    }

    import scala.concurrent.ExecutionContext.Implicits.global
    system.scheduler.scheduleOnce(5 seconds) {
      system.shutdown()
    }
  }

}

class DBConnection extends Actor with ActorLogging {
  override def receive = {
    case _ => log.info(s"${self} received a new DB connection request")
  }
}