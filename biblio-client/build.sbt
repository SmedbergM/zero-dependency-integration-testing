name := "biblio-client"

version := "1.0"

scalaVersion := "2.12.1"

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(
    Defaults.itSettings
  )

libraryDependencies ++= Seq(
  //json4s: https://github.com/json4s/json4s
  "org.json4s" %% "json4s-jackson" % "3.5.2",

  // Basic HTTP client library
  "org.apache.httpcomponents" % "httpclient" % "4.5.3",

  "org.scalatest" %% "scalatest" % "3.0.3" % "it,test"

)

