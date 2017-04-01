package analyticbastard.clluc

import akka.actor.{Props, ActorSystem}
import analyticbastard.clluc.actors.{Rope, Monkey}
import analyticbastard.clluc.definitions.Conf
import Conf._

/**
  * Created by Javier on 01/04/2017.
  */
object Main extends App {
  println("Starting system")
  val system = ActorSystem.create(systemName)

  val rope = system actorOf(Props[Rope], name = "Rope")
  val monkey = system actorOf(Props[Monkey], name = "monkey")
  monkey ! "hello"
  //helloActor ! "buenos dias"
  system.terminate()
  println("System terminated")
}