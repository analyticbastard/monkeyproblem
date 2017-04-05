package analyticbastard.monkeyproblem

import akka.actor.{Actor, ActorPath, ActorRef}
import akka.event.Logging

/**
  * Created by javier on 5/04/17.
  */
case class RelayMonkey(monkeyPath: ActorPath) extends Actor {
  val log = Logging(context.system, this)

  var actorRef: ActorRef = _
  var messagesSoFar: Set[Any] = Set()

  override def receive = {
    case (x: String) => actorRef = sender
    case (x: Any) =>
      if (actorRef != null) {
        if (!(messagesSoFar contains x)) {
          println(x)
          actorRef ! x
        }
        messagesSoFar = messagesSoFar + x
      }
  }
}
