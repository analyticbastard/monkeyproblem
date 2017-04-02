package analyticbastard.monkeyproblem.actors

import akka.actor.Actor
import analyticbastard.monkeyproblem.definitions.Actions._
import analyticbastard.monkeyproblem.definitions.{Direction, Undefined}
import java.time.LocalDateTime

import analyticbastard.monkeyproblem.util.Util._

/**
  * Created by Javier on 01/04/2017.
  */
class Rope extends Actor {
  var currentDirection : Direction = Undefined
  var hangingMonkeysCount : Long = 0
  var lastHoldTime : LocalDateTime = LocalDateTime.MIN

  override def receive = {
    case ObserveHangingMonkeys => sender ! HangingMonkeys(currentDirection, lastHoldTime)
    case Hold(direction) => newMonkeyGotHold(direction)
    case Release => releaseMonkey()
  }

  def releaseMonkey() = {
    sender ! Finished
    hangingMonkeysCount -= 1
    resetDirectionIfThereAreNoHangingMonkeys()
  }

  def resetDirectionIfThereAreNoHangingMonkeys() = changeDirection(Undefined)

  def newMonkeyGotHold(direction: Direction) = {
    setDirectionToThatOfMonkey(direction)
    hangingMonkeysCount += 1
    lastHoldTime = LocalDateTime.now()
    allMonkeysActorRefs(context) ! Hung(sender, lastHoldTime)
  }

  def setDirectionToThatOfMonkey(direction: Direction) = changeDirection(direction)

  def changeDirection(direction: Direction) = if (hangingMonkeysCount == 0) currentDirection = direction
}
