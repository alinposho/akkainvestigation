package zzz.akka.investigation.actors.in.the.cloud.heading

import akka.actor.Props

trait HeadingIndicatorProvider {
  def headingIndicator(): Props = HeadingIndicator()
}
