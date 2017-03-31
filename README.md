# New readme

## How to use the project

## Developer's setup

Download the [DCEVM](https://github.com/dcevm/dcevm/releases) and [Hotswap agent](https://github.com/HotswapProjects/HotswapAgent/releases).

Use the DCEVM installer and replace your JVM runtime or install it as alternative JVM. When runnig
Scala console, make sure you use the command line options `-XXaltjvm=dcevm -javaagent:<hotswap-agent.jar>`
(omit the `altjvm` if you replaced your JVM). For the agent use the absolute path to where you
placed the agent file.

If you are using IntelliJ there is a DCEVM plugin that singnals class reloading. Opening a Scala Console
allows you to re-issue your commands and create objects of the new version of the classes.