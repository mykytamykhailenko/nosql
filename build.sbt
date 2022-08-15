import sbt.addCompilerPlugin

name := """monads"""
organization := "aimprosoft"

version := "1.0-SNAPSHOT"

ThisBuild / libraryDependencies += "org.specs2" %% "specs2-core" % "4.16.1" % Test

val confJavaOption = "-Dconfig.file=conf/test.conf"

lazy val root = (project in file("common"))
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
  .dependsOn(root)
  .settings(
    scalaVersion := "2.13.6",
    Test / fork := true,
    Test / javaOptions += confJavaOption,
    libraryDependencies ++= Seq(
      guice,
      "com.kubukoz" %% "slick-effect" % "0.4.0",
      "com.typesafe.slick" %% "slick" % "3.3.3",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
      "com.h2database" % "h2" % "2.1.214",
      "mysql" % "mysql-connector-java" % "8.0.30"),
    routesImport ++= Seq(
      "com.aimprosoft.common.controllers.Bindable.bindableId",
      "com.aimprosoft.common.model.Id")
  )
  .enablePlugins(PlayScala)

lazy val doobie = (project in file("doobie"))
  .dependsOn(root)
  .settings(
    scalaVersion := "2.13.6",
    Test / fork := true,
    Test / javaOptions += confJavaOption,
    libraryDependencies ++= Seq(
      guice,
      "io.monix" %% "monix" % "3.4.1",
      "dev.zio" %% "zio" % "2.0.0",
      "dev.zio" %% "zio-interop-cats" % "22.0.0.0",
      "org.tpolecat" %% "doobie-core" % "0.13.4", // I had to downgrade Doobie for compatibility with Monix.
      "org.tpolecat" %% "doobie-hikari" % "0.13.4",
      "com.h2database" % "h2" % "2.1.214",
      "mysql" % "mysql-connector-java" % "8.0.30"),
    routesImport ++= Seq(
      "com.aimprosoft.common.controllers.Bindable.bindableId",
      "com.aimprosoft.common.model.Id")
  )
  .enablePlugins(PlayScala)


