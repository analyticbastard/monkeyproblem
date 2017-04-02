package analyticbastard.monkeyproblem.actors

import java.time.LocalDateTime

import akka.actor.{ActorRef, Actor}
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
    case Hung(actorRef, lastHoldTime) => ignoreIfAlreadyHangingOtherwiseCrossOrAbort(actorRef, lastHoldTime)
    case Finished => status = Statuses.Finished
  }

  def ignoreIfAlreadyHangingOtherwiseCrossOrAbort(actorRef: ActorRef, lastHoldTime: LocalDateTime) =
    if (status != Statuses.Hanging)
      cossIfIcouldHoldOrAbortIfOtherMonkeyHeld(actorRef, lastHoldTime)

  def cossIfIcouldHoldOrAbortIfOtherMonkeyHeld(actorRef: ActorRef, lastHoldTime: LocalDateTime) =
    if (actorRef == self) doCrossCanyon()
    else abortJumpingOtherMonkeyWasLuckier(lastHoldTime)

  def tryToJumpAndThenHold(ropeDirection: Direction): Unit = {
    if (isMonkeyGrounded && areHangingMonkeysTravelingTheSameWay(ropeDirection))
      status = Statuses.Jumping
    delayedRun(timeToGetToTheRope)(tryHold)
  }

  def doCrossCanyon() = {
    delayedRun(timeToCross)(() => {
      ropeActorRef(context)  ! Release
    })
    status = Statuses.Hanging
  }

  def abortJumpingOtherMonkeyWasLuckier(lastHoldTime: LocalDateTime) = {
    lastMonkeyRopeHoldTime = lastHoldTime
    status = Statuses.Grounded
  }

  def areHangingMonkeysTravelingTheSameWay(ropeDirection: Direction): Boolean = {
    compatibleDirections(ropeDirection)
  }

  val compatibleDirections: Set[Direction] = Set(direction, Undefined)

  def tryHold() = {
    if (currentTimeMeetsMonkeyTimeSpacing(lastMonkeyRopeHoldTime) && status == Statuses.Jumping)
      ropeActorRef(context) ! Hold(direction)
    else if (didMonkeyAbortJumping)
      tryToJumpAndThenHold(direction)
  }

  def isMonkeyGrounded: Boolean = status == Statuses.Grounded

  def didMonkeyAbortJumping : Boolean = isMonkeyGrounded

}
