package analyticbastard.monkeyproblem

import akka.actor.{ActorSelection, ActorSystem, Props}
import akka.testkit.TestKit
import analyticbastard.monkeyproblem.actors.Monkey
import analyticbastard.monkeyproblem.actors.logic.MonkeyLogic
import analyticbastard.monkeyproblem.definitions.Actions._
import analyticbastard.monkeyproblem.definitions.Conf._
import analyticbastard.monkeyproblem.definitions.Statuses.Finished
import analyticbastard.monkeyproblem.definitions.{East, West}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * Created by javier on 4/04/17.
  */
class MonkeyIntegration extends WordSpecLike
  with BeforeAndAfterAll
  with Matchers {

  val system: ActorSystem = ActorSystem.create(systemName)

  val monkeyLogics = Seq(
    new MonkeyLogic(West, "monkey1"),
    new MonkeyLogic(East, "monkey2"),
    new MonkeyLogic(East, "monkey3"),
    new MonkeyLogic(East, "monkey4"),
    new MonkeyLogic(West, "monkey5")
  )

  val numberOfMonkeys: Int = monkeyLogics.length

  monkeyLogics.foreach((logic) => system actorOf(Props(Monkey(logic)), name = logic.name))

  var selection: ActorSelection = system.actorSelection(s"akka://$systemName/user/*monkey*")
  selection ! Start

  "The bunch of monkeys " must {

    Thread.sleep(numberOfMonkeys*timeToJump + 2*timeToCross + 2000)

    "finish with no exeptions" in {
      assert(monkeyLogics.count((logic) => logic.status == Finished) == numberOfMonkeys)
    }
  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
}
