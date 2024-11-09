ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.5.1"

lazy val root = (project in file("."))
  .settings(
    name := "TimeScraper",
    libraryDependencies += "io.spray" %% "spray-json" % "1.3.6",
    libraryDependencies += "org.scala-lang" %% "toolkit" % "0.4.0",
    libraryDependencies += "com.softwaremill.sttp.client4" %% "core" % "4.0.0-M14"
  )
