ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "websocket",
    organization := "com.github.sophiecollard",
    libraryDependencies ++= List(
      "co.fs2" %% "fs2-core" % "3.9.3",
      "com.beachape" %% "enumeratum" % "1.7.3",
      "com.beachape" %% "enumeratum-circe" % "1.7.3",
      "com.softwaremill.sttp.tapir" %% "tapir-core" % "1.9.2",
      "com.softwaremill.sttp.tapir" %% "tapir-enumeratum" % "1.9.2",
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "1.9.2",
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-client" % "1.9.2",
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % "1.9.2",
      "org.http4s" %% "http4s-client" % "0.23.24",
      "org.http4s" %% "http4s-core" % "0.23.24",
      "org.http4s" %% "http4s-ember-client" % "0.23.24",
      "org.http4s" %% "http4s-ember-server" % "0.23.24",
      "org.typelevel" %% "cats-core" % "2.10.0",
      "org.typelevel" %% "cats-effect-std" % "3.5.2",
      "org.typelevel" %% "log4cats-core" % "2.6.0",
      "org.typelevel" %% "log4cats-slf4j" % "2.6.0"
    )
  )
