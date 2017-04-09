package analyticbastard.monkeyproblem.actors

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, Cancellable, Scheduler}
import akka.event.Logging
import analyticbastard.monkeyproblem.definitions.Actions._
import analyticbastard.monkeyproblem.definitions.Conf._
import analyticbastard.monkeyproblem.definitions.Direction
import analyticbastard.monkeyproblem.definitions.Statuses._
import analyticbastard.monkeyproblem.util.Util._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration


/**
  * Created by Javier on 31/03/2017.
  */
case class Monkey(direction: Direction,
                  timeToCross : Long = timeToCross,
                  timeToJump: Long = timeToJump) extends Actor {
  val log = Logging(context.system, this)
  val scheduler: Scheduler = context.system.scheduler

  val selfName: String = self.path.name

  var status = Grounded
  var jumpTask: Cancellable = _
  var askJumpTask: Cancellable = _
  var askHangTask: Cancellable = _
  var lastMonkeyName: String = selfName
  var lastMonkeyDirection: Direction = _
  var lastHangingMonkeyTime: LocalDateTime = LocalDateTime.MIN
  var shouldAbortJump: Boolean = false

  override def receive = {
    case Start => startSchedulingAJump()
    case WhoIsJumping => sendStatusIfCurrentlyJumping()
    case Jump(senderDirection) => possiblyAbortJump(senderDirection)
    case WhoIsHanging => sendStatusIfCurrentlyHanging()
    case Hang(senderDirection) => setHangingMonkeyDirectionAndResetJumpPolicy(senderDirection)
  }

  def startSchedulingAJump(): Unit = {
    status = Jumping
    allMonkeysActorRefs(context) ! WhoIsJumping
    log.debug(s"Jumping")
    jumpTask = jumpAndPossiblyHoldRope()
    askJumpTask = setUpJumpingMonkeyChecker()
    askHangTask = setUpHangingMonkeyChecker()
  }

  private def setUpJumpingMonkeyChecker() =
    scheduler.schedule(Duration.create(jumpingMonkeyCheckerTime, TimeUnit.MILLISECONDS),
      Duration.create(jumpingMonkeyCheckerTime, TimeUnit.MILLISECONDS)) {
      allMonkeysActorRefs(context) ! WhoIsJumping
    }

  private def setUpHangingMonkeyChecker() =
    scheduler.schedule(Duration.create(hangingMonkeyCheckerTime, TimeUnit.MILLISECONDS),
      Duration.create(hangingMonkeyCheckerTime, TimeUnit.MILLISECONDS)) {
      allMonkeysActorRefs(context) ! WhoIsHanging
    }

  private def sendStatusIfCurrentlyJumping() = if (sender != self && status == Jumping) sender ! Jump(direction)

  private def sendStatusIfCurrentlyHanging() = if (sender != self && status == Hanging) sender ! Hang(direction)

  private def jumpAndPossiblyHoldRope() = {
    scheduler.schedule(Duration.create(timeToJump, TimeUnit.MILLISECONDS), Duration.create(timeToJump, TimeUnit.MILLISECONDS))
    {
      val allowIfHangingMonkeyIsSameDirectionOrItHasBeenLongEnough =
        direction == lastMonkeyDirection || lastMonkeyHangingGivenTimeOrMoreAgo(timeToCross)

      if (!shouldAbortJump && allowIfHangingMonkeyIsSameDirectionOrItHasBeenLongEnough) {
        status = Hanging
        if (!noNeedToJumpAnymoreCancelJumpTask) log.debug("Error when cancelling task")
        log.info(s"Hanging")
        finishAfterCrossing
      } else {
        log.debug("Will try to jump again")
        shouldAbortJump = false
      }
    }
  }

  def noNeedToJumpAnymoreCancelJumpTask(): Boolean = jumpTask.cancel() || jumpTask.isCancelled

  private def finishAfterCrossing: Cancellable =
    scheduler.scheduleOnce(Duration.create(timeToCross, TimeUnit.MILLISECONDS)) {
      finish()
    }

  def possiblyAbortJump(senderDirection: Direction): Unit =
    if (selfName != sender.path.name) shouldAbortJump = shouldAbortJump || possiblyAbortIfThereIsAHangingMonkeyAlreadyOrNot(senderDirection)

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

  private def ifSenderNameLessThanNameThenSenderJumpsAndThisAborts = sender.path.name < selfName

  def setHangingMonkeyDirectionAndResetJumpPolicy(senderDirection: Direction): Unit =
    if (sender != self) {
      val extraTimeToTryJumping: Long = timeToCross + 1000
      if (lastMonkeyHangingGivenTimeOrMoreAgo(extraTimeToTryJumping)) lastHangingMonkeyTime = LocalDateTime.now
      lastMonkeyDirection = senderDirection
      lastMonkeyName = selfName
      shouldAbortJump = possiblyAbortBecauseOfHangingMonkey(senderDirection)
    }

  def lastMonkeyHangingGivenTimeOrMoreAgo(millisSinceHanging: Long): Boolean =
    LocalDateTime.now.minus(millisSinceHanging, ChronoUnit.MILLIS).isAfter(lastHangingMonkeyTime)

  def possiblyAbortBecauseOfHangingMonkey(senderDirection: Direction): Boolean = senderDirection != direction

  def finish(): Unit = {
    askJumpTask.cancel()
    askHangTask.cancel()
    log.info(s"FINISHED crossing [${LocalDateTime.now}] on side ${direction.name}")
    status = Finished
    val extraTimeToPickUpMessages: Int = 50
    scheduler.scheduleOnce(Duration.create(extraTimeToPickUpMessages, TimeUnit.MILLISECONDS)) {
      context.stop(self)
    }
  }
}
