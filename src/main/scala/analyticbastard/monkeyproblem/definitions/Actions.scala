package analyticbastard.monkeyproblem.definitions

/**
  * Created by Javier on 01/04/2017.
  */
object Actions {
  case object Start
  case object WhoIsJumping
  case class Jump(direction: Direction)
  case object WhoIsHanging
  case class Hang(direction: Direction)
}

