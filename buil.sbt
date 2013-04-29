name := "AkkaInvestigation"

version := "1.0"
 
scalaVersion := "2.10.1"

EclipseKeys.withSource := true
 
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  //"org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",// sbt for scala 2.10+
  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
  "com.typesafe.akka" %% "akka-testkit" % "2.2-M3",
  "com.typesafe.akka" %% "akka-actor" % "2.2-M3"
)
