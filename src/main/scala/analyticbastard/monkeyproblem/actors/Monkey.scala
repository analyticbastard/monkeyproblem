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
  var shouldAbort: Boolean = false

  override def receive = {
    case Start => startSchedulingAJump()
    case WhoIsJumping => sendStatusIfCurrentlyJumping()
    case Jump(senderDirection) => possiblyAbortJump(senderDirection)
    case WhoIsHanging => sendStatusIfCurrentlyHanging()
    case Hang(senderDirection) => setHangingMonkeyDirectionAndResetJumpPolicy(senderDirection)
  }

  private def startSchedulingAJump() = {
    log.info(s"Start on side ${direction.opposite.name}")
    jump()
  }

  def jump(): Unit = {
    status = Jumping
    allMonkeysActorRefs(context) ! WhoIsJumping
    log.debug(s"Jumping")
    jumpAndHoldRope()
    setUpJumpingMonkeyChecker()
    setUpHangingMonkeyChecker()
  }

  private def setUpJumpingMonkeyChecker() =
    askJumpTask = scheduler.schedule(Duration.create(jumpingMonkeyCheckerTime, TimeUnit.MILLISECONDS),
      Duration.create(jumpingMonkeyCheckerTime, TimeUnit.MILLISECONDS)) {
      allMonkeysActorRefs(context) ! WhoIsJumping
    }

  private def setUpHangingMonkeyChecker() =
    askHangTask = scheduler.schedule(Duration.create(hangingMonkeyCheckerTime, TimeUnit.MILLISECONDS),
      Duration.create(hangingMonkeyCheckerTime, TimeUnit.MILLISECONDS)) {
      allMonkeysActorRefs(context) ! WhoIsHanging
    }

  private def sendStatusIfCurrentlyJumping() = if (sender != self && status == Jumping) sender ! Jump(direction)

  private def sendStatusIfCurrentlyHanging() = if (sender != self && status == Hanging) sender ! Hang(direction)

  private def jumpAndHoldRope() = {
    jumpTask = scheduler.schedule(Duration.create(timeToJump, TimeUnit.MILLISECONDS), Duration.create(timeToJump, TimeUnit.MILLISECONDS))
    {
      val allowIfHangingMonkeyIsSameDirectionOrItHasBeenLongEnough =
        direction == lastMonkeyDirection || LocalDateTime.now.minus(timeToCross, ChronoUnit.MILLIS).isAfter(lastHangingMonkeyTime)

      if (!shouldAbort && allowIfHangingMonkeyIsSameDirectionOrItHasBeenLongEnough) {
        status = Hanging
        if (!noNeedToJumpAnymoreCancelJumpTask) log.debug("Error when cancelling task")
        log.info(s"Hanging")
        finishAfterCrossing
      } else {
        log.debug("Will try to jump again")
        shouldAbort = false
      }
    }
  }

  def noNeedToJumpAnymoreCancelJumpTask(): Boolean = jumpTask.cancel() || jumpTask.isCancelled

  private def finishAfterCrossing: Cancellable =
    scheduler.scheduleOnce(Duration.create(timeToCross, TimeUnit.MILLISECONDS)) {
      finish()
    }

  def possiblyAbortJump(senderDirection: Direction): Unit =
    if (selfName != sender.path.name) shouldAbort = shouldAbort || possiblyAbortIfThereIsAHangingMonkeyAlreadyOrNot(senderDirection)

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
    if (sender != self && jumpPolicy) {
      lastMonkeyDirection = senderDirection
      lastMonkeyName = selfName
      shouldAbort = possiblyAbortBecauseOfHangingMonkey(senderDirection)
      lastHangingMonkeyTime = LocalDateTime.now
    }

  def possiblyAbortBecauseOfHangingMonkey(senderDirection: Direction): Boolean = senderDirection != direction

  def finish(): Unit = {
    askJumpTask.cancel()
    askHangTask.cancel()
    log.info(s"FINISHED crossing [${LocalDateTime.now}] on side ${direction.name}")
    status = Finished
    context.stop(self)
  }
}
