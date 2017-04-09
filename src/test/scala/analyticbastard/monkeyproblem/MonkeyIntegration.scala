package analyticbastard.monkeyproblem

import akka.actor.{ActorSelection, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import analyticbastard.monkeyproblem.actors.logic.MonkeyLogic
import analyticbastard.monkeyproblem.actors.Monkey
import analyticbastard.monkeyproblem.definitions.Actions._
import analyticbastard.monkeyproblem.definitions.Conf._
import analyticbastard.monkeyproblem.definitions.{East, West}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * Created by javier on 4/04/17.
  */
class MonkeyIntegration extends TestKit(ActorSystem(systemName))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers {

  val numberOfMonkeys = 5
  system actorOf(Props(Monkey(new MonkeyLogic(West))), name = "monkey1")
  system actorOf(Props(Monkey(new MonkeyLogic(East))), name = "monkey2")
  system actorOf(Props(Monkey(new MonkeyLogic(East))), name = "monkey3")
  system actorOf(Props(Monkey(new MonkeyLogic(East))), name = "monkey4")
  system actorOf(Props(Monkey(new MonkeyLogic(West))), name = "monkey5")

  var selection: ActorSelection = system.actorSelection(s"akka://$systemName/user/*monkey*")
  selection ! Start

  "The bunch of monkeys " must {

    Thread.sleep(numberOfMonkeys*timeToJump + numberOfMonkeys*timeToCross + 1000)

    "finish with no exeptions" in {
      succeed // finish without execption
    }
  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
}
