package analyticbastard.clluc.definitions

/**
  * Created by Javier on 01/04/2017.
  */

sealed trait Direction { def name: String }

case object Westbound extends Direction { val name = "Westbound" }
case object Eastbound extends Direction { val name = "Eastbound" }
case object Undefined extends Direction { val name = "Undefined" }
