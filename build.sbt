name := "perimeterx-challange"
version := "1.0"
scalaVersion := "2.11.8"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.10"
libraryDependencies += "com.typesafe.akka" %% "akka-http-core" % "2.4.10"
libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % "2.4.10"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0"
libraryDependencies += "com.typesafe.akka" %% "akka-http-experimental" % "2.4.10"

lazy val root = (project in file(".")).
  settings(
    name := "perimeterx-challange",
    version := "1.0",
    scalaVersion := "2.11.8",
    mainClass in Compile := Some("Boot")
  )

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}