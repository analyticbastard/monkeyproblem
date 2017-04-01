package analyticbastard.clluc.util

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Javier on 01/04/2017.
  */
object Util {
  def delayedRun[T](howLong: Long)(f: () => T) : Unit = {
    Future {
      Thread.sleep(howLong)
      f()
    }
  }
}
