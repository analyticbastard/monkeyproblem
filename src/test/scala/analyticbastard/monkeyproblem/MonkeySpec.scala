package analyticbastard.monkeyproblem

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import analyticbastard.monkeyproblem.actors.Monkey
import analyticbastard.monkeyproblem.definitions.Actions._
import analyticbastard.monkeyproblem.definitions.Conf._
import analyticbastard.monkeyproblem.definitions.West
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * Created by javier on 4/04/17.
  */
class MonkeySpec extends TestKit(ActorSystem(systemName))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll
  with Matchers {

  private val direction = West
  val monkeyWest: ActorRef = system.actorOf(Props(Monkey(direction)), name = "monkeyWest")
  val relayMonkey: ActorRef = system.actorOf(Props(RelayMonkey(monkeyWest.path)), name = "monkeyMock")

  relayMonkey ! "test_start"
  monkeyWest ! Start

  "A Monkey " must {
    "send WhoIsJumping requests " in {
      expectMsg(WhoIsJumping)
    }

    "send either WhoIsHanging or Hang" in {
      expectMsg(WhoIsHanging)
    }

    Thread.sleep(timeToJump + timeToCross + 1000)

    "finish with no exeptions" in {
      succeed // finish without execption
    }
  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
}
