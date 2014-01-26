package zzz.akka.agents.oficial.doc

import akka.agent._

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm._
//import scala.language.postfixOps

/** This feature has been deprecated in Akka version 2.3 */
@RunWith(classOf[JUnitRunner])
class AgentsAndTransactionsTest extends FunSuite {

  def transfer(from: Agent[Int], to: Agent[Int], amount: Int): Boolean = {
    atomic { txn ⇒
      if (from.get < amount) false
      else {
        from send (_ - amount)
        to send (_ + amount)
        true
      }
    }
  }

  // This feature has been deprecated in Akka 2.3
  test("An example illustrating how Agents and STM work together") {
    val from = Agent(100)
    val to = Agent(20)
    val ok = transfer(from, to, 50)

    assert(ok === true, "The transfer was successful")
    assert(Await.result(from.future, 3.seconds) === 50) // Notice that we still need to use the await trick to get the latest update
    assert(Await.result(to.future, 3.seconds) === 70)
  }

}