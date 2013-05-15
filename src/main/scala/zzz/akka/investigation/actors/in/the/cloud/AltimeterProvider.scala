package zzz.akka.investigation.actors.in.the.cloud

import akka.actor.Props

trait AltimeterProvider {
	def altimeter: Props = Altimeter()
}