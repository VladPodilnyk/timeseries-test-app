val V = new {
  val distage         = "1.0.10"
  val logstage        = distage
  val scalatest       = "3.2.11"
  val scalacheck      = "1.15.4"
  val http4s          = "0.22.11"
  val doobie          = "0.13.4"
  val zio             = "1.0.13"
  val zioCats         = "2.5.1.0"
  val kindProjector   = "0.13.1"
  val circe           = "0.14.1"
  val circeDerivation = "0.13.0-M5"
  val fs2             = "2.5.10"
  val requests        = "0.7.0"
}

val Deps = new {
  val scalatest  = "org.scalatest" %% "scalatest" % V.scalatest
  val scalacheck = "org.scalacheck" %% "scalacheck" % V.scalacheck

  val distageCore    = "io.7mind.izumi" %% "distage-core" % V.distage
  val distageConfig  = "io.7mind.izumi" %% "distage-extension-config" % V.distage
  val distageRoles   = "io.7mind.izumi" %% "distage-framework" % V.distage
  val distageDocker  = "io.7mind.izumi" %% "distage-framework-docker" % V.distage
  val distageTestkit = "io.7mind.izumi" %% "distage-testkit-scalatest" % V.distage
  val logstageSlf4j  = "io.7mind.izumi" %% "logstage-adapter-slf4j" % V.logstage

  val http4sDsl    = "org.http4s" %% "http4s-dsl" % V.http4s
  val http4sServer = "org.http4s" %% "http4s-blaze-server" % V.http4s
  val http4sCirce  = "org.http4s" %% "http4s-circe" % V.http4s

  val circeDerivation = "io.circe" %% "circe-derivation" % V.circeDerivation
  val circeParser     = "io.circe" %% "circe-parser" % V.circe

  val doobie         = "org.tpolecat" %% "doobie-core" % V.doobie
  val doobiePostgres = "org.tpolecat" %% "doobie-postgres" % V.doobie
  val doobieHikari   = "org.tpolecat" %% "doobie-hikari" % V.doobie

  val kindProjector = "org.typelevel" % "kind-projector" % V.kindProjector cross CrossVersion.full

  val zio     = "dev.zio" %% "zio" % V.zio
  val zioCats = "dev.zio" %% "zio-interop-cats" % V.zioCats

  val fs2Core = "co.fs2" %% "fs2-core" % V.fs2
  val fs2Io   = "co.fs2" %% "fs2-io" % V.fs2

  val grpcNetty          = "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion
  val scalapbRuntime     = "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
  val scalapbRuntimeGrpc = "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion

  val requests = "com.lihaoyi" %% "requests" % V.requests
}

inThisBuild(
  Seq(
    scalaVersion := "2.13.6",
    version      := "1.0.0",
    organization := "DigitalNomads",
  )
)

lazy val protobuf = project
  .in(file("protobuf"))
  .settings(
    libraryDependencies ++= Seq(
      Deps.grpcNetty,
      Deps.scalapbRuntime,
      Deps.scalapbRuntimeGrpc,
    ),
    PB.targets in Compile := Seq(scalapb.gen() -> (sourceManaged in Compile).value),
    PB.protoSources in Compile += (baseDirectory in LocalRootProject).value / "protobuf/src/main/protobuf",
  )

lazy val leaderboard = project
  .in(file("."))
  .settings(
    name := "timeseries",
    libraryDependencies ++= Seq(
      Deps.distageCore,
      Deps.distageRoles,
      Deps.distageConfig,
      Deps.logstageSlf4j,
      Deps.distageDocker,
      Deps.distageTestkit % Test,
      Deps.scalatest % Test,
      Deps.scalacheck % Test,
      Deps.http4sDsl,
      Deps.http4sServer,
      Deps.http4sCirce,
      Deps.circeDerivation,
      Deps.doobie,
      Deps.doobiePostgres,
      Deps.doobieHikari,
      Deps.zio,
      Deps.zioCats,
      Deps.fs2Core,
      Deps.fs2Io,
      Deps.requests % Test,
      Deps.circeParser % Test,
    ),
    addCompilerPlugin(Deps.kindProjector),
    scalacOptions -= "-Xfatal-warnings",
    scalacOptions += "-Xsource:3",
    scalacOptions += "-P:kind-projector:underscore-placeholders",
    scalacOptions += "-Wmacros:after",
    scalacOptions ++= Seq(
      s"-Xmacro-settings:product-name=${name.value}",
      s"-Xmacro-settings:product-version=${version.value}",
      s"-Xmacro-settings:product-group=${organization.value}",
      s"-Xmacro-settings:scala-version=${scalaVersion.value}",
      s"-Xmacro-settings:scala-versions=${crossScalaVersions.value.mkString(":")}",
      s"-Xmacro-settings:sbt-version=${sbtVersion.value}",
      s"-Xmacro-settings:git-repo-clean=${git.gitUncommittedChanges.value}",
      s"-Xmacro-settings:git-branch=${git.gitCurrentBranch.value}",
      s"-Xmacro-settings:git-described-version=${git.gitDescribedVersion.value.getOrElse("")}",
      s"-Xmacro-settings:git-head-commit=${git.gitHeadCommit.value.getOrElse("")}",
    ),
  )
  .dependsOn(protobuf)
  //.enablePlugins(UniversalPlugin)

ThisBuild / resolvers += Resolver.sonatypeRepo("snapshots")
