import org.scalajs.linker.interface.OutputPatterns
import org.scalajs.linker.interface.ESVersion
import org.scalajs.linker.interface.OutputPatterns

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "chessboard",
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.13" % Test,
    libraryDependencies += "xyz.bluepitaya" %%% "common-utils" % "1.0",
    libraryDependencies += "org.typelevel" %%% "cats-effect" % "3.3.14",
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
      .withOutputPatterns(OutputPatterns.fromJSFile("%s.js"))
      .withESFeatures(_.withESVersion(ESVersion.ES2021))
    },
    Compile / fastLinkJS / scalaJSLinkerOutputDirectory := baseDirectory.value / "ui/src/scalajs/",
  )
  .enablePlugins(ScalaJSPlugin)
