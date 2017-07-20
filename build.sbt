name := """commit-viewer"""
organization := "com.codacy"

version := "1.0-SNAPSHOT"

resolvers += "Eclipse repositories" at "https://repo.eclipse.org/service/local/repositories/egit-releases/content/"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(resolvers += "Eclipse Maven" at "https://repo.eclipse.org/content/groups/releases/")
  .settings(libraryDependencies ++= Seq(
    evolutions,
    jdbc,

    "com.typesafe.play" %% "play-slick" % "3.0.0",
    "com.typesafe.play" %% "play-slick-evolutions" % "3.0.0",
    "org.xerial" % "sqlite-jdbc" % "3.19.3",

    "com.github.pathikrit" %% "better-files" % "3.0.0",
    "org.eclipse.jgit" % "org.eclipse.jgit" % "4.8.0.201706111038-r",
    "org.eclipse.mylyn.github" % "org.eclipse.egit.github.core" % "4.6.0.201612231935-r",
    "com.google.code.gson" % "gson" % "2.6.2",

    "org.webjars" % "jquery" % "2.1.1",
    "org.webjars" % "bootstrap" % "3.2.0",

    "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test
  ))

scalaVersion := "2.12.2"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.codacy.controllers._"

// Adds additional packages into conf/routes
play.sbt.routes.RoutesKeys.routesImport += "di.binders._"
