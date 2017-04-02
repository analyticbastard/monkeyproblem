import analyticbastard.monkeyproblem.definitions.Actions.Started
import analyticbastard.monkeyproblem.definitions.Undefined
import analyticbastard.monkeyproblem.definitions.Conf._
import akka.actor.Props
import analyticbastard.monkeyproblem.actors.{Monkey, Rope}
import analyticbastard.monkeyproblem.definitions.Westbound
import akka.actor.ActorSystem
import analyticbastard.monkeyproblem.definitions.Conf

val system = ActorSystem.create(Conf.systemName)

val rope = system actorOf(Props[Rope], name = "rope")
val monkey = system actorOf(Props(new Monkey(Westbound)), name = "monkey")
val monkey2 = system actorOf(Props(new Monkey(Westbound)), name = "monkey2")

var selection = system.actorSelection(s"akka://$systemName/user/*monkey*")
selection ! Started(Undefined)