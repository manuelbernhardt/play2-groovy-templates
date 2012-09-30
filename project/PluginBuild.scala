import sbt._
import Keys._

object PluginBuild extends Build {

  val buildVersion = "1.5.3-SNAPSHOT"

  val delvingReleases = "Delving Releases Repository" at "http://development.delving.org:8081/nexus/content/repositories/releases"
  val delvingSnapshots = "Delving Snapshot Repository" at "http://development.delving.org:8081/nexus/content/repositories/snapshots"
  val delvingRepository = if(buildVersion.endsWith("SNAPSHOT")) delvingSnapshots else delvingReleases

  val dependencies = Seq(
    "play"                           %% "play"                         % "2.0.3",
    "eu.delving"                     %  "groovy-templates-engine"      % "0.7.1",
    "commons-io"                     %  "commons-io"                   % "2.0",
    "com.googlecode.htmlcompressor"  %  "htmlcompressor"               % "1.5.2",
    "com.google.javascript"          %  "closure-compiler"             % "r1043",
    "com.yahoo.platform.yui"         %  "yuicompressor"                % "2.4.6"
  )

  lazy val root = Project(
    id = "root",
    base = file(".")
  ).settings(
    publish := { }
  ).aggregate(templatesSbtPlugin, main)

  lazy val templatesSbtPlugin = Project(
    id="groovy-templates-sbt-plugin",
    base=file("src/sbt-plugin")
  ).settings(
    sbtPlugin := true,

    organization := "eu.delving",

    version := buildVersion,

    publishTo := Some(delvingRepository),

    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),

    publishMavenStyle := true
  )

  lazy val main = Project(
    id = "groovy-templates-plugin",
    base = file("src/templates-plugin")).settings(
      organization := "eu.delving",

      version := buildVersion,

      resolvers += "jahia" at "http://maven.jahia.org/maven2",

      resolvers += delvingReleases,

      resolvers += delvingSnapshots,

      libraryDependencies ++= dependencies,

      publishTo := Some(delvingRepository),

      credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),

      publishMavenStyle := true
    ).dependsOn(templatesSbtPlugin)

}
