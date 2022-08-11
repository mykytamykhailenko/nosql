import sbt.addCompilerPlugin

name := """monads"""
organization := "aimprosoft"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

lazy val common = (project in file("common"))
  .settings(
    scalaVersion := "2.13.6",
    libraryDependencies ++= Seq(
      guice,
      "org.typelevel" %% "cats-core" % "2.8.0"),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full)
  )
  .enablePlugins(PlayScala)

lazy val slick = (project in file("slick"))
  .dependsOn(common)
  .settings(
    scalaVersion := "2.13.6",
    libraryDependencies ++= Seq(
      guice,
      "com.kubukoz" %% "slick-effect" % "0.4.0",
      "com.typesafe.slick" %% "slick" % "3.3.3",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
      "mysql" % "mysql-connector-java" % "8.0.30"),
    routesImport ++= Seq(
      "com.aimprosoft.common.controllers.Bindable.bindableId",
      "com.aimprosoft.common.model.Id")
  )
  .enablePlugins(PlayScala)

lazy val doobie = (project in file("doobie"))
  .dependsOn(common)
  .settings(
    scalaVersion := "2.13.6",
    libraryDependencies ++= Seq(
      guice,
      "io.monix" %% "monix" % "3.4.1",
      "org.tpolecat" %% "doobie-core" % "0.13.4", // I had to downgrade Doobie for compatibility with Monix.
      "org.tpolecat" %% "doobie-hikari" % "0.13.4",
      "mysql" % "mysql-connector-java" % "8.0.30"),
    routesImport ++= Seq(
      "com.aimprosoft.common.controllers.Bindable.bindableId",
      "com.aimprosoft.common.model.Id")
  )
  .enablePlugins(PlayScala)



scalaVersion := "2.13.6"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test

/*
libraryDependencies ++= Seq(
  // "io.monix" %% "monix" % "3.4.1",
  "org.typelevel" %% "cats-core" % "2.8.0",
  "com.kubukoz" %% "slick-effect" % "0.4.0",
  "com.typesafe.slick" %% "slick" % "3.3.3",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",

  // Start with this one
  "org.tpolecat" %% "doobie-core" % "1.0.0-RC1",
  "mysql" % "mysql-connector-java" % "8.0.29",
  "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC1",
  "org.tpolecat" %% "doobie-specs2" % "1.0.0-RC1" % Test,
  "org.tpolecat" %% "doobie-scalatest" % "1.0.0-RC1" % Test)*/

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "aimprosoft.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "aimprosoft.binders._"


