ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

val baseDependencies = Seq()

lazy val root = (project in file("."))
  .settings(
    name := "chessboard",
    libraryDependencies += baseDependencies
  )
  .enablePlugins(ScalaJSPlugin)
