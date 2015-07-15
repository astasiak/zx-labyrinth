name := """ZX labyrinth"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.4"

scalacOptions += "-target:jvm-1.8"

libraryDependencies ++= Seq(
  ws, // Play's web services module
  "com.typesafe.akka" %% "akka-actor" % "2.3.4",
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.4",
  "org.webjars" % "bootstrap" % "3.2.0",
  "org.webjars" % "flot" % "0.8.0",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.4" % "test",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test",
  "com.google.guava" % "guava" % "18.0",
  "com.google.code.findbugs" % "jsr305" % "3.0.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.1.0",
  "org.mongodb" %% "casbah" % "2.8.1",
  "org.mindrot" % "jbcrypt" % "0.3m"
//  "org.slf4j" % "slf4j-simple" % "1.5.6"
)

//fork in run := true

EclipseKeys.createSrc := EclipseCreateSrc.All
