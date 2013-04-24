package zzz.akka.investigation

// We need these three components to create an actor
import akka.actor.{ Actor, Props, ActorSystem }

// Our Actor

class BadShakespeareanActor extends Actor {

  override def receive = {
    case "Good Morning" =>
      println("Him: Forsooth 'tis the 'morn, but mourneth for thou doest I do!")
    case "You're terrible" =>
      println("Him: Yup")
  }
}

object BadShakespeareanMain {

  val actorSystem = ActorSystem("BadShakespearean")
  val shakespearActor = actorSystem.actorOf(Props[BadShakespeareanActor])

  // We'll use this utility method to talk with out Actor
  def send(msg: String) {
    println("Me " + msg)
    shakespearActor ! msg
    Thread.sleep(100)
  }
  
  // And our driver
  def main(args: Array[String]) {
    send("Good Morning")
    send("You're terrible")
    actorSystem.shutdown()
  }
}