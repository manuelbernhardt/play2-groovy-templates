import sbt._
import Keys._

object PluginBuild extends Build {

  val dependencies = Seq(
    "play" %% "play" % "2.0-RC1-SNAPSHOT",
    "play" %% "groovy-template-engine" % "0.1-SNAPSHOT",
    "org.codehaus.groovy" % "groovy" % "1.8.5",
    "commons-collections" % "commons-collections" % "3.2.1",
    "commons-lang" % "commons-lang" % "2.6"
  )

  val main = Project(
    id = "groovy-templates",
    base = file(".")).settings(
      organization := "play",

      libraryDependencies ++= dependencies,

      publishMavenStyle := false
    )

}
