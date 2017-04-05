# Monkey problem
[![Build Status](https://travis-ci.org/analyticbastard/monkeyproblem.svg?branch=master)](https://travis-ci.org/analyticbastard/monkeyproblem)

## Only monkey actors system

The design is focused on independent actors which decide for themselves what to do upon the
information they receive via the asynchronous message passing system of Akka.

Since there is no central authority to resolve disputes between these pairs, emphasis must be
put on the protocol to effectively deal with conflicting situations. Here, I use the local
computer time so that a monkey agent can decide whether to abort the jump to grab the rope.
If no other monkey jumped before and the 1 second to grab the rope has passed, the agent will
change its state to effectively hold it and cross the Canyon.

## Protocol

Each monkey needs to jump to get hold of the rope. It takes 1 second to do this.
While it is jumping it might notice that other monkeys started jumping before him or that there
is a monkey hanging from the rope. If a monkey is hanging from the rope, the monkey checks
whether it goes in the same direction, and aborts if it is not.

On the other hand, when jumping to grab the rope the protocol is as follows:

If we are in the same direction than the last known monkey on the rope
check if other monkeys in the same direction jumped before.
 
### Jumping policy

Here we define what jumping before means. In this case, it coincides with the monkey name (path)
so that everything can look clear upon a glance.

When a monkey grabs the rope, the last jumping monkey name is reset, because it is updated
with the winning candidate to grab the rope.

## Implementation

The implementation uses Akka. There are Akka scheduled timers to poll for jumping monkeys
and hanging monkeys.

## Self-documented code

I am a strong advocate of self-documented code and a critic of commented code. I will
not quote Uncle Bob in this but suffice to say, comments are another part of software that must be
maintained. 

The methods are short and specific and the variable and method names
are long, hinting at what they do in the most precise way. The code is then read as prose.

## Developer's setup

Download the [DCEVM](https://github.com/dcevm/dcevm/releases) and [Hotswap agent](https://github.com/HotswapProjects/HotswapAgent/releases).

Use the DCEVM installer and replace your JVM runtime or install it as alternative JVM. When runnig
Scala console, make sure you use the command line options `-XXaltjvm=dcevm -javaagent:<hotswap-agent.jar>`
(omit the `altjvm` if you replaced your JVM). For the agent use the absolute path to where you
placed the agent file.

If you are using IntelliJ there is a DCEVM plugin that singnals class reloading. Opening a Scala Console
allows you to re-issue your commands and create objects of the new version of the classes.
