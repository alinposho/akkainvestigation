package zzz.akka.investigation.actors.in.the.cloud.bathroom

import akka.actor._
import akka.agent._

import java.util.concurrent.TimeUnit

import scala.collection.immutable.Queue
import scala.concurrent.duration._

sealed abstract class Gender
case object Male extends Gender
case object Female extends Gender
case class GenderAndTime(gender: Gender, peakDuration: Duration, count: Int)

object Bathroom {
  // States of the FSM
  sealed trait State
  case object Vacant extends State
  case object Occupied extends State

  // The data for our FSM
  sealed trait Data
  case class InUse(by: ActorRef, atTimeMillis: Long, queue: Queue[ActorRef]) extends Data
  case object NotInUse extends Data

  // Messages to and from the FSM
  case object IWannaUseTheBathroom
  case object YouCanUseTheBathroomNow
  case class Finished(gender: Gender)

  // helper function to update the counter
  def updateCounter(male: Agent[GenderAndTime],
                    female: Agent[GenderAndTime],
                    gender: Gender,
                    duration: Duration): Unit = {
    def incCounter(old: GenderAndTime): GenderAndTime = {
      val GenderAndTime(oldgender, peak, counter) = old
      GenderAndTime(oldgender, duration.max(peak), counter + 1)
    }

    gender match {
      case Male => male send (incCounter(_))
      case Female => female send (incCounter(_))
    }
  }

}

import Bathroom._

class Bathroom(femaleCounter: Agent[GenderAndTime], maleCounter: Agent[GenderAndTime])
  extends Actor
  with FSM[State, Data] {

  startWith(Vacant, NotInUse)

  when(Vacant) {
    case Event(IWannaUseTheBathroom, _) => {
      sender ! YouCanUseTheBathroomNow
      goto(Occupied) using InUse(by = sender, atTimeMillis = System.currentTimeMillis, queue = Queue())
    }
  }

  when(Occupied) {
    case Event(IWannaUseTheBathroom, data: InUse) => {
      stay using data.copy(queue = data.queue.enqueue(sender))
    }
    case Event(Finished(gender), data: InUse) if sender == data.by => {
      updateCounter(maleCounter, femaleCounter, gender, Duration(System.currentTimeMillis() - data.atTimeMillis, TimeUnit.SECONDS))
      if(data.queue.isEmpty) {
        goto(Vacant)
      } else {
    	  val (next, q) = data.queue.dequeue
    	  next ! YouCanUseTheBathroomNow
    	  stay using InUse(next, System.currentTimeMillis(), q)
      }
    }
  }
  
  initialize

}



