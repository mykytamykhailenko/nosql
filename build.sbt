name := """monads"""
organization := "aimprosoft"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.6"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test

addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full)
addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % "2.8.0",
  "org.typelevel" %% "cats-free" % "2.8.0",
  "com.kubukoz" %% "slick-effect" % "0.4.0",
  "com.typesafe.slick" %% "slick" % "3.3.3",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",

  // Start with this one
  "org.tpolecat" %% "doobie-core" % "1.0.0-RC1",
  "mysql" % "mysql-connector-java" % "8.0.29",
  "net.codingwell" %% "scala-guice" % "5.1.0",
  "org.tpolecat" %% "doobie-hikari" % "1.0.0-RC1",
  "org.tpolecat" %% "doobie-specs2" % "1.0.0-RC1" % Test,
  "org.tpolecat" %% "doobie-scalatest" % "1.0.0-RC1" % Test)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "aimprosoft.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "aimprosoft.binders._"

play.sbt.routes.RoutesKeys.routesImport ++= Seq("com.aimprosoft.controllers.Bindable._", "com.aimprosoft.model.Id")
