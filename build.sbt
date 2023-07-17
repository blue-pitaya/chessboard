import org.scalajs.linker.interface.ESVersion
import org.scalajs.linker.interface.OutputPatterns
import org.scalajs.linker.interface.ModuleSplitStyle

ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"
Global / scalacOptions += "-Ymacro-annotations"
Global / scalacOptions += "-Wunused:imports"

val Http4sVersion = "0.23.20"
val CirceVersion = "0.14.5"
val MunitVersion = "0.7.29"
val LogbackVersion = "1.4.8"
val MunitCatsEffectVersion = "1.0.7"
val MonocleVersion = "3.1.0"

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .withoutSuffixFor(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    name := "chessboard-core",
    libraryDependencies += "org.typelevel" %%% "cats-core" % "2.8.0",
    libraryDependencies += "org.typelevel" %%% "cats-effect" % "3.3.14",
    libraryDependencies += "io.circe" %%% "circe-generic" % CirceVersion,
    libraryDependencies += "io.circe" %%% "circe-parser" % CirceVersion
  )
  .jsSettings(
    scalaJSLinkerConfig ~=
      (_.withModuleKind(ModuleKind.ESModule)
        .withESFeatures(_.withESVersion(ESVersion.ES2021))
        .withSourceMap(false)),
    scalaJSUseMainModuleInitializer := false
  )

lazy val root = (project in file("."))
  .dependsOn(core.js)
  .settings(
    name := "chessboard",
    libraryDependencies += "com.raquo" %%% "laminar" % "16.0.0",
    libraryDependencies += "com.raquo" %%% "waypoint" % "7.0.0",
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.13" % Test,
    libraryDependencies += "org.typelevel" %%% "cats-core" % "2.8.0",
    libraryDependencies += "org.typelevel" %%% "cats-effect" % "3.3.14",
    libraryDependencies += "dev.bluepitaya" %%% "laminar-dragging" % "1.1",
    libraryDependencies += "org.http4s" %%% "http4s-circe" % Http4sVersion,
    libraryDependencies += "io.circe" %%% "circe-generic" % CirceVersion,
    libraryDependencies += "io.laminext" %%% "websocket" % "0.16.0",
    libraryDependencies += "io.laminext" %%% "websocket-circe" % "0.16.0",
    libraryDependencies ++=
      Seq(
        "dev.optics" %%% "monocle-core" % MonocleVersion,
        "dev.optics" %%% "monocle-macro" % MonocleVersion
      ),
    libraryDependencies ++=
      Seq(
        "org.http4s" %%% "http4s-client" % Http4sVersion,
        "org.http4s" %%% "http4s-dom" % "0.2.9",
        "org.http4s" %%% "http4s-dsl" % Http4sVersion
      ),
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withOutputPatterns(OutputPatterns.fromJSFile("%s.js"))
        .withESFeatures(_.withESVersion(ESVersion.ES2021))
    },
    scalaJSUseMainModuleInitializer := true,
    Compile / fastLinkJS / scalaJSLinkerOutputDirectory :=
      baseDirectory.value / "ui/sccode/",
    Compile / fullLinkJS / scalaJSLinkerOutputDirectory :=
      baseDirectory.value / "ui/sccode/"
  )
  .enablePlugins(ScalaJSPlugin)

lazy val api = (project in file("api"))
  .dependsOn(core.jvm)
  .settings(
    name := "chessboard-api",
    libraryDependencies ++=
      Seq(
        "org.http4s" %% "http4s-ember-server" % Http4sVersion,
        "org.http4s" %% "http4s-ember-client" % Http4sVersion,
        "org.http4s" %% "http4s-dsl" % Http4sVersion,
        "org.http4s" %% "http4s-circe" % Http4sVersion,
        "io.circe" %% "circe-generic" % CirceVersion,
        "org.scalameta" %% "munit" % MunitVersion % Test,
        "org.typelevel" %% "munit-cats-effect-3" % MunitCatsEffectVersion %
          Test,
        "ch.qos.logback" % "logback-classic" % LogbackVersion % Runtime,
        // "org.scalameta" %% "svm-subs" % "20.2.0"
        "dev.optics" %%% "monocle-core" % MonocleVersion,
        "dev.optics" %%% "monocle-macro" % MonocleVersion
      )
  )
