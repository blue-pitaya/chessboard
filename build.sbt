import org.scalajs.linker.interface.ESVersion
import org.scalajs.linker.interface.OutputPatterns
import org.scalajs.linker.interface.ModuleSplitStyle

ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    scalacOptions := Seq("-Wunused:imports"),
    name := "chessboard",
    libraryDependencies += "com.raquo" %%% "laminar" % "15.0.0-M7",
    libraryDependencies += "com.raquo" %%% "waypoint" % "6.0.0-M4",
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.13" % Test,
    libraryDependencies += "org.typelevel" %%% "cats-core" % "2.8.0",
    libraryDependencies += "org.typelevel" %%% "cats-effect" % "3.3.14",
    libraryDependencies += "dev.bluepitaya" %%% "laminar-dragging" % "1.0",
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("sccode")))
      // .withOutputPatterns(OutputPatterns.fromJSFile("%s.js"))
      // .withESFeatures(_.withESVersion(ESVersion.ES2021))
    },
    scalaJSUseMainModuleInitializer := true
    // Compile / fastLinkJS / scalaJSLinkerOutputDirectory :=
    //  baseDirectory.value / "ui/",
    // Compile / fullLinkJS / scalaJSLinkerOutputDirectory :=
    //  baseDirectory.value / "ui/"
  )
  .enablePlugins(ScalaJSPlugin)

val http4sVersion = "0.23.22"

lazy val api = (project in file("api")).settings(
  scalacOptions := Seq("-Wunused:imports"),
  name := "chessboard-api",
  libraryDependencies ++=
    Seq(
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion
    )
)
