package analyticbastard.monkeyproblem

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.TestKit
import analyticbastard.monkeyproblem.actors.Monkey
import analyticbastard.monkeyproblem.actors.logic.MonkeyLogic
import analyticbastard.monkeyproblem.definitions.Actions.Start
import analyticbastard.monkeyproblem.definitions.Conf._
import analyticbastard.monkeyproblem.definitions.Statuses.{Hanging, Finished}
import analyticbastard.monkeyproblem.definitions.{East, West}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * Created by Javier on 09/04/2017.
  */
class MonkeyLogicSpec extends TestKit(ActorSystem(systemName))
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers
  with MockFactory {

  val monkey0: String = "monkey0"
  val monkey1: String = "monkey1"
  val monkey2: String = "monkey2"
  val monkeyLogic = new MonkeyLogic(West, monkey1)
  val monkeyActor1: ActorRef = system.actorOf(Props(Monkey(monkeyLogic)), monkey1)
  monkeyActor1 ! Start(true)

  "The Monkey's logic" must {
    "abort jumping when a monkey with a monkey1 value less than his is jumping" in {
      assert(monkeyLogic.ifSenderNameLessThanNameThenSenderJumpsAndThisAborts(monkey0))
    }

    "not abort jumping when a monkey with a monkey1 value less than his is jumping" in {
      assert(!monkeyLogic.ifSenderNameLessThanNameThenSenderJumpsAndThisAborts(monkey2))
    }

    "set the Finished status when finish is called" in {
      monkeyLogic.finish()
      assert(monkeyLogic.status == Finished)
    }

    "in the absence of a monkey on the rope, check only the jumping policy" in {
      assert(monkeyLogic.possiblyAbortIfThereIsAHangingMonkeyAlreadyOrNot(West, monkey0) ==
        monkeyLogic.possiblyAbortIfThereIsAHangingMonkeyAlreadyOrNot(West, monkey0))
    }
  }

  "On the presence of a crossing monkey in direction West, the logic" must {
    "if the other jumping monkey (sender) is in the same direction, abort depending on jumping policy" in {
      monkeyLogic.setHangingMonkeyDirectionAndResetJumpPolicy(West)

      assert(monkeyLogic.possiblyAbortIfThereIsAHangingMonkeyAlreadyOrNot(West, monkey0))
      assert(!monkeyLogic.possiblyAbortIfThereIsAHangingMonkeyAlreadyOrNot(West, monkey2))
    }

    "not abort if the other jumping monkey (sender) is not in the same direction, independently on jumping policy" in {
      assert(!monkeyLogic.possiblyAbortIfThereIsAHangingMonkeyAlreadyOrNot(East, monkey0))
      assert(!monkeyLogic.possiblyAbortIfThereIsAHangingMonkeyAlreadyOrNot(East, monkey2))
    }
  }

  "On the presence of a crossing monkey in direction East, the logic" must {
    "if the other jumping monkey (sender) is in the same direction (but not current monkey, which is west), abort independently on jumping policy" in {
      monkeyLogic.setHangingMonkeyDirectionAndResetJumpPolicy(East)

      assert(monkeyLogic.possiblyAbortIfThereIsAHangingMonkeyAlreadyOrNot(East, monkey0))
      assert(monkeyLogic.possiblyAbortIfThereIsAHangingMonkeyAlreadyOrNot(East, monkey2))
    }

    "abort if the other jumping monkey (sender) is not in the same direction depending on jumping policy" in {
      assert(monkeyLogic.possiblyAbortIfThereIsAHangingMonkeyAlreadyOrNot(West, monkey0))
      assert(!monkeyLogic.possiblyAbortIfThereIsAHangingMonkeyAlreadyOrNot(West, monkey2))
    }
  }

  "The last monkey hanging time" must {
    "be more ancient than 0 milliseconds in the past" in {
      assert(monkeyLogic.lastMonkeyHangingGivenTimeOrMoreAgo(0))
    }

    "but within a nontrivial amount of seconds" in {
      assert(!monkeyLogic.lastMonkeyHangingGivenTimeOrMoreAgo(timeToJump))
    }
  }

  "The monkey " must {
    "abort jumping if either there is another jumping monkey meeting jumping policy or time has not yet elapsed" in {
      monkeyLogic.lastHangingMonkeyDirection = null
      monkeyLogic.shouldAbortJump = true
      monkeyLogic.lastHangingMonkeyTime = LocalDateTime.now.minus(timeToCross+100, ChronoUnit.MILLIS)
      monkeyLogic.jumpAndPossiblyHoldRope()
      assert(!monkeyLogic.shouldAbortJump)

      monkeyLogic.shouldAbortJump = false
      monkeyLogic.lastHangingMonkeyTime = LocalDateTime.now.minus(0, ChronoUnit.MILLIS)
      monkeyLogic.jumpAndPossiblyHoldRope()
      assert(!monkeyLogic.shouldAbortJump)

      monkeyLogic.shouldAbortJump = false
      monkeyLogic.lastHangingMonkeyTime = LocalDateTime.now.minus(timeToCross+100, ChronoUnit.MILLIS)
      monkeyLogic.jumpAndPossiblyHoldRope()
      assert(monkeyLogic.status == Hanging)
    }
  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

}
