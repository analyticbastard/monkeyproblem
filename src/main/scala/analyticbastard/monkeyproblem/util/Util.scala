package analyticbastard.monkeyproblem.util

import java.time.LocalDateTime
import java.time.temporal.ChronoField

import akka.actor.{ActorContext, ActorSelection}
import akka.event.LoggingAdapter
import analyticbastard.monkeyproblem.definitions.Conf._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Javier on 01/04/2017.
  */
object Util {
  def l(log: LoggingAdapter, s: String) = log.info(s)

  def delayedRun[T](howLong: Long)(f: => T) : Unit = {
    Future {
      Thread.sleep(howLong)
      f
    }
  }

  def allMonkeysActorRefs(context: ActorContext): ActorSelection = {
    context.actorSelection(s"akka://$systemName/user/*$monkeyBaseName*")
  }

  def ropeActorRef(context: ActorContext): ActorSelection = {
    context.actorSelection(s"akka://$systemName/user/*$ropeBaseName*")
  }

  def currentTimeMeetsMonkeyTimeSpacing(lastMonkeyRopeHoldTime : LocalDateTime): Boolean = {
    val now = LocalDateTime.now()
    val nowMinusTimeToGetToTheRope = now.minus(timeToGetToTheRope, ChronoField.MILLI_OF_DAY.getBaseUnit)

    lastMonkeyRopeHoldTime.isBefore(nowMinusTimeToGetToTheRope)
  }
}
