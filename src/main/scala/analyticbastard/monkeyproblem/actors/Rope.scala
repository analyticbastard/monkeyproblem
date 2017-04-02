package analyticbastard.monkeyproblem.actors

import akka.actor.{Actor, ActorSelection}
import analyticbastard.monkeyproblem.definitions.Actions._
import analyticbastard.monkeyproblem.definitions.{Direction, Undefined}
import java.time.LocalDateTime

import analyticbastard.monkeyproblem.util.Util._

/**
  * Created by Javier on 01/04/2017.
  */
class Rope extends Actor {
  var currentDirection : Direction = Undefined
  var hangingMonkeysCount : Long = 0
  var lastHoldTime : LocalDateTime = LocalDateTime.MIN

  override def receive = {
    case ObserveHangingMonkeys => sender ! HangingMonkeys(currentDirection, lastHoldTime)

    case Hold(direction) =>
      if (hangingMonkeysCount == 0) currentDirection = direction
      hangingMonkeysCount += 1
      lastHoldTime = LocalDateTime.now()
      allMonkeysActorRefs(context) ! Hung(sender, lastHoldTime)

    case Release =>
      sender ! Finished
      hangingMonkeysCount -= 1
      if (hangingMonkeysCount == 0) currentDirection = Undefined
  }
}
