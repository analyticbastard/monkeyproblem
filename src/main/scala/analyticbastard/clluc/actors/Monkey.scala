package analyticbastard.clluc.actors

import java.time.LocalDateTime
import java.time.temporal.ChronoField

import akka.actor.{ActorSelection, Actor}
import analyticbastard.clluc.definitions.Conf._
import analyticbastard.clluc.util.Util._
import analyticbastard.clluc.definitions.Actions._
import analyticbastard.clluc.definitions.{Undefined, Statuses, Direction}


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
      val selection: ActorSelection = context.actorSelection(s"akka://$systemName/user/*$ropeBaseName*")
      selection ! Release
    })
    status = Statuses.Hanging
  }

  def tryToJumpAndThenHold(ropeDirection: Direction): Unit = {
    if (status == Statuses.Grounded && Set(direction, Undefined)(ropeDirection)) status = Statuses.Jumping
    delayedRun(timeToGetToTheRope)(tryHold)
  }

  def tryHold() = {
    val now = LocalDateTime.now()
    val nowMinusTimeToGetToTheRope = now.minus(timeToGetToTheRope, ChronoField.MILLI_OF_DAY.getBaseUnit)

    if (lastMonkeyRopeHoldTime.isBefore(nowMinusTimeToGetToTheRope) && status == Statuses.Jumping) {
      val selection: ActorSelection = context.actorSelection(s"akka://$systemName/user/*$ropeBaseName*")
      selection ! Hold(direction)
    } else
      tryToJumpAndThenHold(direction)
  }
}
