ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

val baseDependencies = Seq()

val testDependencties = Seq()

lazy val root = (project in file("."))
  .settings(
    name := "chessboard",
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.13" % Test,
  )
  .enablePlugins(ScalaJSPlugin)
