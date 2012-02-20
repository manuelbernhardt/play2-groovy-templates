import sbt._
import Keys._

object PluginBuild extends Build {

  val dependencies = Seq(
    "play" %% "play" % "2.0-RC3-SNAPSHOT",
    "play" %% "groovy-template-engine" % "0.1-SNAPSHOT",
    "org.codehaus.groovy" % "groovy" % "1.8.5",
    "com.jamonapi" % "jamon" % "2.7", 
    "commons-collections" % "commons-collections" % "3.2.1",
    "commons-lang" % "commons-lang" % "2.6",
    "commons-io" % "commons-io" % "2.0"
  )

  val main = Project(
    id = "groovy-templates",
    base = file(".")).settings(
      organization := "play",

      resolvers += "jahia" at "http://maven.jahia.org/maven2",

      libraryDependencies ++= dependencies,

      publishMavenStyle := false
    )

}
