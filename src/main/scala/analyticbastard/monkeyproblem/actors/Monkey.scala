package analyticbastard.monkeyproblem.actors

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, Cancellable, Scheduler}
import akka.event.Logging
import analyticbastard.monkeyproblem.actors.logic.MonkeyLogic
import analyticbastard.monkeyproblem.definitions.Actions._
import analyticbastard.monkeyproblem.definitions.Conf._
import analyticbastard.monkeyproblem.definitions.Direction
import analyticbastard.monkeyproblem.util.Util._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration


/**
  * Created by Javier on 31/03/2017.
  */
case class Monkey(monkeyLogic: MonkeyLogic) extends Actor {
  val log = Logging(context.system, this)
  val scheduler: Scheduler = context.system.scheduler

  val name: String = self.path.name

  var jumpTask: Cancellable = _
  var askJumpTask: Cancellable = _
  var askHangTask: Cancellable = _

  override def receive = {
    case Start(test) => startSchedulingAJump(test)
    case Start => startSchedulingAJump(false)
    case WhoIsJumping => sendJump()
    case Jump(senderDirection) => possiblyAbortJump(senderDirection)
    case WhoIsHanging => sendHang()
    case Hang(senderDirection) => setHangingMonkeyDirectionAndResetJumpPolicy(senderDirection)
  }

  def startSchedulingAJump(test: Boolean): Unit = {
    monkeyLogic.self = this
    monkeyLogic.name = name
    if (!test) {
      allMonkeysActorRefs(context) ! WhoIsJumping
      log.debug(s"Jumping")
      jumpTask = jumpAndPossiblyHoldRope()
      askJumpTask = setUpJumpingMonkeyChecker()
      askHangTask = setUpHangingMonkeyChecker()
    }
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

  private def sendJump() = if (sender != self && monkeyLogic.isJumping) sender ! Jump(monkeyLogic.direction)

  private def sendHang() = if (sender != self && monkeyLogic.isHanging) sender ! Hang(monkeyLogic.direction)

  private def jumpAndPossiblyHoldRope() =
    scheduler.schedule(Duration.create(timeToJump, TimeUnit.MILLISECONDS),
      Duration.create(timeToJump, TimeUnit.MILLISECONDS))(monkeyLogic.jumpAndPossiblyHoldRope())

  def noNeedToJumpAnymoreCancelJumpTask(): Boolean = jumpTask == null || jumpTask.cancel() || jumpTask.isCancelled

  def finishAfterCrossing: Cancellable =
    scheduler.scheduleOnce(Duration.create(timeToCross, TimeUnit.MILLISECONDS)) {
      finish(false)
    }

  def possiblyAbortJump(senderDirection: Direction): Unit =
    if (name != sender.path.name) monkeyLogic.possiblyAbortJump(senderDirection, sender.path.name)

  def setHangingMonkeyDirectionAndResetJumpPolicy(senderDirection: Direction): Unit =
    if (!monkeyLogic.checkConflict(senderDirection)) {
      if (sender != self) monkeyLogic.setHangingMonkeyDirectionAndResetJumpPolicy(senderDirection)
    } else finish(true)

  def info(text: String): Unit =  log.info(text)

  def debug(text: String): Unit =  log.debug(text)

  def finish(conflict: Boolean): Unit = {
    askJumpTask.cancel()
    askHangTask.cancel()
    monkeyLogic.finish()
    if (conflict) log.info("CONFLICT!!!")
    val extraTimeToPickUpMessages: Int = 50
    scheduler.scheduleOnce(Duration.create(extraTimeToPickUpMessages, TimeUnit.MILLISECONDS)) {
      context.stop(self)
    }
  }
}
