name := "krawlr"

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= List (
  "com.ning" % "async-http-client" % "1.7.19",
  "org.slf4j" % "slf4j-simple" % "1.7.12",
  "com.typesafe.akka" %% "akka-actor" % "2.3.11",
  "org.jsoup" % "jsoup" % "1.8.1",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.11" % "test",
  "org.scalatest" % "scalatest_2.11" % "2.2.5" % "test"
)