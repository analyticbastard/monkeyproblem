package analyticbastard.monkeyproblem.definitions

/**
  * Created by Javier on 01/04/2017.
  */

sealed trait Direction {
  def name: String
  def opposite: Direction
}

case object West extends Direction {
  val name = "West"
  override def opposite: Direction = East
}

case object East extends Direction {
  val name = "East"
  override def opposite: Direction = West
}
