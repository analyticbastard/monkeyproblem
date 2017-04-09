package analyticbastard.monkeyproblem

import akka.actor.{ActorSystem, Props}
import analyticbastard.monkeyproblem.actors.Monkey
import analyticbastard.monkeyproblem.actors.logic.MonkeyLogic
import analyticbastard.monkeyproblem.definitions.Actions.Start
import analyticbastard.monkeyproblem.definitions.Conf._
import analyticbastard.monkeyproblem.definitions.{East, West}

/**
  * Created by Javier on 01/04/2017.
  */
object Main extends App {
  println("Starting system")
  val system = ActorSystem.create(systemName)

  system actorOf(Props(Monkey(new MonkeyLogic(West))), name = "monkey1")
  system actorOf(Props(Monkey(new MonkeyLogic(East))), name = "monkey2")
  system actorOf(Props(Monkey(new MonkeyLogic(East))), name = "monkey3")

  system.actorSelection(s"akka://$systemName/user/*monkey*") ! Start

  private val extraTime = 6000
  Thread.sleep(timeToCross*2 + timeToJump*3 + extraTime)

  system.terminate()
  println("System terminated")
}