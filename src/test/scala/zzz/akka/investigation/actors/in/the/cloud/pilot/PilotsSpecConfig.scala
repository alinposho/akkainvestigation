package zzz.akka.investigation.actors.in.the.cloud.pilot

object PilotsSpecConfig {
  val copilotName = "Mary"
  val pilotName = "Mark"
  val configStr = s"""
  					zzz.akka.avionics.flightcrew.copilotName = "$copilotName"
  					zzz.akka.avionics.flightcrew.pilotName = "$pilotName"
  					"""
}
