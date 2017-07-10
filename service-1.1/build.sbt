name := "biblio-service-1.1"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.3" % "test",

  // Logging
  "com.typesafe.scala-logging"  %% "scala-logging"  % "3.5.0",
  "ch.qos.logback"              % "logback-classic"  % "1.2.1", // SLF4J implementation

  //json4s: https://github.com/json4s/json4s
  "org.json4s" %% "json4s-jackson" % "3.5.2",

  "org.http4s" %% "http4s-dsl" % "0.15.13",
  "org.http4s" %% "http4s-blaze-server" % "0.15.13"
)

parallelExecution in Test := false