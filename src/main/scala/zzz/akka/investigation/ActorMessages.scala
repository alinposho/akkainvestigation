package zzz.akka.investigation

import akka.actor.{ Actor, Props, ActorSystem }

case class Gamma(g: String)
case class Beta(b: String, g: Gamma)
case class Alpha(b1: Beta, b2: Beta)

class MyActor extends Actor {

  def receive() = {
    case "Hello" =>
      println("Hi")
    case 42 =>
      println("I don't know the question. Go ask Eath Mark II.")
    case s: String =>
      println("You sent me the string: \"" + s + "\"")
    case Alpha(Beta(beta1, Gamma(gamma1)), Beta(beta2, Gamma(gamma2))) =>
      println(s"beta1: %s, beta2: %s, gamma1: %s, gamma2: %s".format(beta1, beta2, gamma1, gamma2))
    case _ =>
      println("Huh?")
  }
}
object ActorMessages {

  def main(args: Array[String]) {
    val system = ActorSystem("MySystem")
    val actor = system.actorOf(Props[MyActor])

    actor ! "Hello"
    actor ! "Some String"
    actor ! 42
    actor ! Alpha(Beta("beta1", Gamma("gamma1")), Beta("beta2", Gamma("gamma2")))
    actor ! 456.5F
    actor tell ("""This is a message sent using the "tell(msg, null)" method """, null)
    // The following call will compile and run, however, it's deprecated!
    //actor tell """This is a message sent using the "tell" method """

    system.shutdown()
  }
}