lazy val akkaHttpVersion = "10.1.10"
lazy val akkaVersion = "2.6.0"

// -------------------------------------------------------------------------------------------------------------------
// Root Project
// -------------------------------------------------------------------------------------------------------------------
lazy val root = (project in file("."))
  .settings(
    inThisBuild(List(
      organization := "com.techmonal",
      scalaVersion := "2.12.10"
    )),
    name := "spark-datasets"
  )
  .aggregate(common, engine, web)
  .dependsOn(common, engine, web)

// -------------------------------------------------------------------------------------------------------------------
// Common Module
// -------------------------------------------------------------------------------------------------------------------
lazy val common = project.in(file("modules/common"))
  .settings(name := "common")
  .settings(libraryDependencies ++= commonLibraryDependencies)

// -------------------------------------------------------------------------------------------------------------------
// Web Module
// -------------------------------------------------------------------------------------------------------------------
lazy val web = project.in(file("modules/web"))
  .settings(name := "web")
  .aggregate(common).dependsOn(common)
  .settings(libraryDependencies ++= akkaLibraryDependencies ++ commonLibraryDependencies)

// -------------------------------------------------------------------------------------------------------------------
// Web Module
// -------------------------------------------------------------------------------------------------------------------
lazy val engine = project.in(file("modules/engine"))
  .settings(name := "engine")
  .aggregate(common).dependsOn(common)
  .settings(libraryDependencies ++= sparkLibraryDependencies)

lazy val commonLibraryDependencies = Seq(
  "com.softwaremill.sttp.client" %% "spray-json" % "2.0.0-RC5",
  "ch.qos.logback" % "logback-classic" % "1.2.3",

  "org.scalatest" %% "scalatest" % "3.0.8" % Test
)

lazy val akkaLibraryDependencies = Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,

  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
)

lazy val sparkLibraryDependencies = Seq(
  "org.apache.spark" %% "spark-sql" % "2.4.4",
  //"com.softwaremill.sttp.client" %% "core" % "2.0.0-RC5",
  //"com.softwaremill.sttp.client" %% "spray-json" % "2.0.0-RC5",
  //"com.softwaremill.sttp.client" %% "play-json" % "2.0.0-RC5"
  //"com.softwaremill.sttp.client" %% "json4s" % "2.0.0-RC5",
  //"org.json4s" %% "json4s-native" % "3.6.0",
)
