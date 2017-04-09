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

  val selfName: String = self.path.name

  var jumpTask: Cancellable = _
  var askJumpTask: Cancellable = _
  var askHangTask: Cancellable = _

  override def receive = {
    case Start => startSchedulingAJump()
    case WhoIsJumping => sendJump()
    case Jump(senderDirection) => possiblyAbortJump(senderDirection)
    case WhoIsHanging => sendHang()
    case Hang(senderDirection) => setHangingMonkeyDirectionAndResetJumpPolicy(senderDirection)
  }

  def startSchedulingAJump(): Unit = {
    monkeyLogic.self = this
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

  private def sendJump() = if (sender != self && monkeyLogic.isJumping) sender ! Jump(monkeyLogic.direction)

  private def sendHang() = if (sender != self && monkeyLogic.isHanging) sender ! Hang(monkeyLogic.direction)

  private def jumpAndPossiblyHoldRope() =
    scheduler.schedule(Duration.create(timeToJump, TimeUnit.MILLISECONDS),
      Duration.create(timeToJump, TimeUnit.MILLISECONDS))(monkeyLogic.jumpAndPossiblyHoldRope())

  def noNeedToJumpAnymoreCancelJumpTask(): Boolean = jumpTask.cancel() || jumpTask.isCancelled

  def finishAfterCrossing: Cancellable =
    scheduler.scheduleOnce(Duration.create(timeToCross, TimeUnit.MILLISECONDS)) {
      finish()
    }

  def possiblyAbortJump(senderDirection: Direction): Unit =
    if (selfName != sender.path.name) monkeyLogic.possiblyAbortJump(senderDirection)

  def setHangingMonkeyDirectionAndResetJumpPolicy(senderDirection: Direction): Unit =
    if (sender != self) monkeyLogic.setHangingMonkeyDirectionAndResetJumpPolicy(senderDirection)

  def finish(): Unit = {
    askJumpTask.cancel()
    askHangTask.cancel()
    monkeyLogic.finish()
    val extraTimeToPickUpMessages: Int = 50
    scheduler.scheduleOnce(Duration.create(extraTimeToPickUpMessages, TimeUnit.MILLISECONDS)) {
      context.stop(self)
    }
  }
}
