package analyticbastard.monkeyproblem.actors

import java.time.LocalDateTime

import akka.actor.Actor
import analyticbastard.monkeyproblem.definitions.Conf._
import analyticbastard.monkeyproblem.util.Util._
import analyticbastard.monkeyproblem.definitions.Actions._
import analyticbastard.monkeyproblem.definitions.{Undefined, Statuses, Direction}


/**
  * Created by Javier on 31/03/2017.
  */
case class Monkey(direction: Direction,
                  timeToCross : Long = timeToCross,
                  timeToGetToTheRope: Long = timeToGetToTheRope) extends Actor {
  var status = Statuses.Grounded
  var lastMonkeyRopeHoldTime : LocalDateTime = LocalDateTime.MIN

  override def receive = {
    case Started(ropeDirection) => tryToJumpAndThenHold(ropeDirection)

    case Hung(actorRef, lastHoldTime) =>
      if (actorRef == self) {
        doCrossCanyon()
      } else {
        lastMonkeyRopeHoldTime = lastHoldTime
        status = Statuses.Grounded
      }

    case Finished => status = Statuses.Finished
  }

  def doCrossCanyon() = {
    delayedRun(timeToCross)(() => {
      ropeActorRef(context)  ! Release
    })
    status = Statuses.Hanging
  }

  def tryToJumpAndThenHold(ropeDirection: Direction): Unit = {
    if (status == Statuses.Grounded && Set(direction, Undefined)(ropeDirection))
      status = Statuses.Jumping
    delayedRun(timeToGetToTheRope)(tryHold)
  }

  def tryHold() = {
    if (currentTimeMeetsMonkeyTimeSpacing(lastMonkeyRopeHoldTime) && status == Statuses.Jumping) {
      ropeActorRef(context) ! Hold(direction)
    } else
      tryToJumpAndThenHold(direction)
  }
}
