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
                  timeToCross : Long = timeToCross,
                  timeToJump: Long = timeToJump) {

  var status = Jumping

  var lastMonkeyName: String = _
  var lastMonkeyDirection: Direction = _
  var lastHangingMonkeyTime: LocalDateTime = LocalDateTime.MIN
  var shouldAbortJump: Boolean = false
  var self: Monkey = _

  def jumpAndPossiblyHoldRope() = {
    val allowIfHangingMonkeyIsSameDirectionOrItHasBeenLongEnough =
      direction == lastMonkeyDirection || lastMonkeyHangingGivenTimeOrMoreAgo(timeToCross)

    if (!shouldAbortJump && allowIfHangingMonkeyIsSameDirectionOrItHasBeenLongEnough) {
      status = Hanging
      if (!self.noNeedToJumpAnymoreCancelJumpTask) self.log.debug("Error when cancelling task")
      self.log.info(s"Hanging")
      self.finishAfterCrossing
    } else {
      self.log.debug("Will try to jump again")
      shouldAbortJump = false
    }
  }

  def possiblyAbortJump(senderDirection: Direction): Unit =
    shouldAbortJump = shouldAbortJump || possiblyAbortIfThereIsAHangingMonkeyAlreadyOrNot(senderDirection)

  def possiblyAbortIfThereIsAHangingMonkeyAlreadyOrNot(senderDirection: Direction): Boolean =
    if (lastMonkeyDirection == null) jumpPolicy
    else {
      if (direction == lastMonkeyDirection) {
        if (senderDirection == lastMonkeyDirection) jumpPolicy
        else false
      } else {
        if (senderDirection == lastMonkeyDirection) true
        else jumpPolicy
      }
    }

  private def jumpPolicy = ifSenderNameLessThanNameThenSenderJumpsAndThisAborts

  private def ifSenderNameLessThanNameThenSenderJumpsAndThisAborts = self.sender.path.name < self.selfName

  def setHangingMonkeyDirectionAndResetJumpPolicy(senderDirection: Direction): Unit =
  {
    val extraTimeToTryJumping: Long = timeToCross + 1000
    if (lastMonkeyHangingGivenTimeOrMoreAgo(extraTimeToTryJumping)) lastHangingMonkeyTime = LocalDateTime.now
    lastMonkeyDirection = senderDirection
    lastMonkeyName = self.selfName
    shouldAbortJump = possiblyAbortBecauseOfHangingMonkey(senderDirection)
  }

  def lastMonkeyHangingGivenTimeOrMoreAgo(millisSinceHanging: Long): Boolean =
    LocalDateTime.now.minus(millisSinceHanging, ChronoUnit.MILLIS).isAfter(lastHangingMonkeyTime)

  def possiblyAbortBecauseOfHangingMonkey(senderDirection: Direction): Boolean = senderDirection != direction

  def isJumping: Boolean = status == Jumping

  def isHanging: Boolean = status == Hanging

  def finish(): Unit = {
    self.log.info(s"FINISHED crossing on side ${direction.name}")
    status = Finished
  }
}
