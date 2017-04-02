import analyticbastard.monkeyproblem.definitions.Actions.Start
import analyticbastard.monkeyproblem.definitions.Conf._
import akka.actor.Props
import analyticbastard.monkeyproblem.actors.Monkey
import analyticbastard.monkeyproblem.definitions.{Conf, East, West}
import akka.actor.ActorSystem

val system = ActorSystem.create(Conf.systemName)
val monkey1 = system actorOf(Props(new Monkey(West)), name = "monkey1")
val monkey2 = system actorOf(Props(new Monkey(East)), name = "monkey2")
val monkey3 = system actorOf(Props(new Monkey(East)), name = "monkey3")
val monkey4 = system actorOf(Props(new Monkey(East)), name = "monkey4")
val monkey5 = system actorOf(Props(new Monkey(West)), name = "monkey5")

var selection = system.actorSelection(s"akka://$systemName/user/*monkey*")
selection ! Start