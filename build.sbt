name := "AkkaInvestigation"

version := "1.0"
 
scalaVersion := "2.10.2"

EclipseKeys.withSource := true
 
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "junit" % "junit" % "4.8" % "test", 
  "org.scalatest" %% "scalatest" % "1.9.1" % "test",
  "com.typesafe.akka" %% "akka-testkit" % "2.3-M2",
  "com.typesafe.akka" %% "akka-actor" % "2.3-M2",
  "com.typesafe.akka" %% "akka-agent" % "2.3-M2"
)
