package analyticbastard.monkeyproblem.util

import akka.actor.{ActorContext, ActorSelection}
import analyticbastard.monkeyproblem.definitions.Conf._

/**
  * Created by Javier on 01/04/2017.
  */
object Util {
  def allMonkeysActorRefs(context: ActorContext): ActorSelection = {
    context.actorSelection(s"akka://$systemName/user/*$monkeyBaseName*")
  }
}
