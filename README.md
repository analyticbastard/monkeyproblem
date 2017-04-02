# New readme

## The speed of light problem

This design reinforces the Monkey class in the sense that it contains the control logic of
what a monkey does. It tries to emulate a physical system of observers which use the information
they receive to make decisions. In this case, it is Akka which provides the ether over which
the information flows. Therefore, we are bound to its speed. If the speed of passing messages
is less than that of an monkey when deciding to hold the rope, it will get hold of it before it
is notified another monkey had already done it milliseconds ago.

This is analogous to the spped of light of light and the maximum information speed. In a concurrent
system working on the Earth and the Sun, the events would take up to 8 minutes to reach both ends,
and the logic would need to match the scenario (reset to a previous state, etc).

In this case, I use the trick of porting a little bit of logic to the rope, so that it gets to
decide whether to send the `Held` message to the monkey or not. The other possibility is to
complicate the messaging protocol a little bit more and divide the `Hold` message in two: A hold
attempt and a hold. The try to hold message, in which the monkey reserves the rope by means of the rope
telling all the monkeys trying to hold the rope the time in which the previous attempt was made,
so it is the monkey the one who decides to hold if the previous attempt was made more than 1 sec ago.

In this case, at least for this commit, I prefer to leave the protocol and the code simpler and
let the rope decide whether to send the hold confirmation to the monkey.

## Developer's setup

Download the [DCEVM](https://github.com/dcevm/dcevm/releases) and [Hotswap agent](https://github.com/HotswapProjects/HotswapAgent/releases).

Use the DCEVM installer and replace your JVM runtime or install it as alternative JVM. When runnig
Scala console, make sure you use the command line options `-XXaltjvm=dcevm -javaagent:<hotswap-agent.jar>`
(omit the `altjvm` if you replaced your JVM). For the agent use the absolute path to where you
placed the agent file.

If you are using IntelliJ there is a DCEVM plugin that singnals class reloading. Opening a Scala Console
allows you to re-issue your commands and create objects of the new version of the classes.