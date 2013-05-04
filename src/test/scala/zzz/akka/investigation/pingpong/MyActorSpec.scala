package zzz.akka.investigation.pingpong

import MyActor._
import akka.testkit.TestKit
import akka.actor.ActorSystem
import org.scalatest.matchers.MustMatchers
import org.scalatest.WordSpec
import org.scalatest.BeforeAndAfterAll
import org.scalatest.ParallelTestExecution

class MyActorSpec extends TestKit(ActorSystem("MyActorSpec"))
  with WordSpec
  with MustMatchers
  with BeforeAndAfterAll
  with ParallelTestExecution {

  override def afterAll() { system.shutdown() }

}