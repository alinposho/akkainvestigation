package zzz.akka.actor.routing

import com.typesafe.config.ConfigFactory
import akka.actor._
import akka.routing.FromConfig
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

object DifferentRoutingStrategies {

  def main(args: Array[String]) {
    val system = ActorSystem("RoutingStrategisSystem")
    // In order for this to work, the configuration file "application.conf" has to be in the classpath and
    // it has to contain the configuration for the "DatabaseConnectionRouter" under "akka { actor { configuration {...}}}"
    // otherwise it won't work. On further inspection one might find it possible to customize that.
    val roundRobinRouter = system.actorOf(Props[DBConnection].withRouter(FromConfig()), "RoundRobinRouter")
    val smallestMailboxRouter = system.actorOf(Props[DBConnection].withRouter(FromConfig()), "SmallestMaiboxRouter")
    
    println("Round robin routing strategy")
    sendRequestsTo(roundRobinRouter)
    
    Thread.sleep(2000)
    
    println("\n\nSmallest mailbox routing strategy")
    sendRequestsTo(smallestMailboxRouter)
    
    import scala.concurrent.ExecutionContext.Implicits.global
    system.scheduler.scheduleOnce(5 seconds) {
      system.shutdown()
    }
  }

  private def sendRequestsTo(router: ActorRef): Unit = {
    for (no <- 1 to 40) yield {
      router ! "GetMeNewConnection" + no
    }
  }

}

class DBConnection extends Actor with ActorLogging {
  override def receive = {
    case connection => 
      Thread.sleep((100 * Math.random()).toInt)
      log.info(s"${self} received a new DB connection request ${connection}")
  }
}