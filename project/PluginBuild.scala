import sbt._
import Keys._

object PluginBuild extends Build {

  val buildVersion = "1.2-SNAPSHOT"

  val delvingReleases = "Delving Releases Repository" at "http://development.delving.org:8081/nexus/content/repositories/releases"
  val delvingSnapshots = "Delving Snapshot Repository" at "http://development.delving.org:8081/nexus/content/repositories/snapshots"
  val delvingRepository = if(buildVersion.endsWith("SNAPSHOT")) delvingSnapshots else delvingReleases

  val dependencies = Seq(
    "play"                  %% "play"                         % "2.0",
    "eu.delving"            %  "groovy-templates-engine"      % "0.6",
    "commons-io"            %  "commons-io"                   % "2.0"
  )

  val main = Project(
    id = "groovy-templates-plugin",
    base = file(".")).settings(
      organization := "eu.delving",

      version := buildVersion,

      resolvers += "jahia" at "http://maven.jahia.org/maven2",

      resolvers += delvingReleases,

      resolvers += delvingSnapshots,

      libraryDependencies ++= dependencies,

      publishTo := Some(delvingRepository),

      credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),

      publishMavenStyle := true
    )

}
