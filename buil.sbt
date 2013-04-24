name := "AkkaInvestigation"

version := "1.0"
 
scalaVersion := "2.10.1"

EclipseKeys.withSource := true
 
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.2-M3"
)
