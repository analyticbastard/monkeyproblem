import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "javier.monkeyproblem",
      scalaVersion := "2.12.1",
      version      := "0.1.0-SNAPSHOT",
      coverageEnabled := true
    )),
    parallelExecution in Test := false,
    name := "monkeyproblem",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % Test,
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % "2.4.17",
      "com.typesafe.akka" %% "akka-testkit" % "2.4.17")
)
