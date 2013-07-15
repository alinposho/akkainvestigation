package zzz.akka.investigation.actors.in.the.cloud.flight.attendant

object FlightAttendantPathChecker {
 
  def main(args: Array[String]) {
    val system = akka.actor.ActorSystem("PlaneSimulation")
    val lead = system.actorOf(LeadFlightAttendant(), "LeadFlightAttendant")
    Thread.sleep(2000)
    system.shutdown()
  }
  
}