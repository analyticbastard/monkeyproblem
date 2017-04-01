package analyticbastard.clluc.definitions

import java.time.LocalDateTime

import akka.actor.ActorRef
import analyticbastard.clluc.definitions.Direction

/**
  * Created by Javier on 01/04/2017.
  */
object Actions {
  case object ObserveHangingMonkeys
  case class Jump(direction: Direction)
  case class Hold(direction: Direction)
  case object Release

  case class HangingMonkeys(direction: Direction, lastHoldTime: LocalDateTime)

  case class Hung(actorRef: ActorRef, lastHoldTime: LocalDateTime)
  case class Started(direction: Direction)
  case object Finished
}

