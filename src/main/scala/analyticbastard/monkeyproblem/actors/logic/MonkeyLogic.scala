package analyticbastard.monkeyproblem.actors.logic

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import analyticbastard.monkeyproblem.actors.Monkey
import analyticbastard.monkeyproblem.definitions.Conf._
import analyticbastard.monkeyproblem.definitions.Direction
import analyticbastard.monkeyproblem.definitions.Statuses._

/**
  * Created by Javier on 09/04/2017.
  */
class MonkeyLogic(var direction: Direction,
                  var name: String = "",
                  timeToCross : Long = timeToCross,
                  timeToJump: Long = timeToJump) {

  var status = Jumping

  var lastHangingMonkeyDirection: Direction = _
  var lastHangingMonkeyTime: LocalDateTime = LocalDateTime.MIN
  var shouldAbortJump: Boolean = false
  var self: Monkey = _

  def jumpAndPossiblyHoldRope() = {
    val allowIfHangingMonkeyIsSameDirectionOrItHasBeenLongEnough =
      direction == lastHangingMonkeyDirection || lastMonkeyHangingGivenTimeOrMoreAgo(timeToCross)

    if (!shouldAbortJump && allowIfHangingMonkeyIsSameDirectionOrItHasBeenLongEnough) {
      status = Hanging
      if (!self.noNeedToJumpAnymoreCancelJumpTask) self.debug("Error when cancelling task")
      self.info("Hanging")
      self.finishAfterCrossing
    } else {
      self.debug("Will try to jump again")
      shouldAbortJump = false
    }
  }

  def possiblyAbortJump(senderDirection: Direction, senderName: String): Unit =
    shouldAbortJump = shouldAbortJump || possiblyAbortIfThereIsAHangingMonkeyAlreadyOrNot(senderDirection, senderName)

  def possiblyAbortIfThereIsAHangingMonkeyAlreadyOrNot(senderDirection: Direction, senderName: String): Boolean =
    if (lastHangingMonkeyDirection == null) jumpPolicy(senderName)
    else {
      if (direction == lastHangingMonkeyDirection) {
        if (senderDirection == lastHangingMonkeyDirection) jumpPolicy(senderName)
        else false
      } else {
        if (senderDirection == lastHangingMonkeyDirection) true
        else jumpPolicy(senderName)
      }
    }

  private def jumpPolicy(senderName: String) = ifSenderNameLessThanNameThenSenderJumpsAndThisAborts(senderName)

  def ifSenderNameLessThanNameThenSenderJumpsAndThisAborts(senderName: String) = senderName < name

  def setHangingMonkeyDirectionAndResetJumpPolicy(senderDirection: Direction): Unit =
  {
    val extraTimeToTryJumping: Long = timeToCross + 1000
    if (lastMonkeyHangingGivenTimeOrMoreAgo(extraTimeToTryJumping)) lastHangingMonkeyTime = LocalDateTime.now
    lastHangingMonkeyDirection = senderDirection
    shouldAbortJump = possiblyAbortBecauseOfHangingMonkey(senderDirection)
  }

  def checkConflict(senderDirection: Direction) = status == Hanging && senderDirection != direction

  def lastMonkeyHangingGivenTimeOrMoreAgo(millisSinceHanging: Long): Boolean =
    LocalDateTime.now.minus(millisSinceHanging, ChronoUnit.MILLIS).isAfter(lastHangingMonkeyTime)

  def possiblyAbortBecauseOfHangingMonkey(senderDirection: Direction): Boolean = senderDirection != direction

  def isJumping: Boolean = status == Jumping

  def isHanging: Boolean = status == Hanging

  def finish(): Unit = {
    self.info(s"FINISHED crossing on side ${direction.name}")
    status = Finished
  }
}
