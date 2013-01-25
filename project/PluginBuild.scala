import sbt._
import Keys._

object PluginBuild extends Build {

  val buildVersion = "1.6-SNAPSHOT"


  val delvingReleases = "Delving Releases Repository" at "http://development.delving.org:8081/nexus/content/repositories/releases"
  val delvingSnapshots = "Delving Snapshot Repository" at "http://development.delving.org:8081/nexus/content/repositories/snapshots"
  val delvingRepository = if(buildVersion.endsWith("SNAPSHOT")) delvingSnapshots else delvingReleases

  val dependencies = Seq(
    "play"                           %% "play"                         % "2.1-SNAPSHOT",
    "play"                           %% "play-java"                    % "2.1-SNAPSHOT",
    "play"                           %% "templates"                    % "2.1-SNAPSHOT",
    "eu.delving"                     %  "groovy-templates-engine"      % "0.7.1",
    "commons-io"                     %  "commons-io"                   % "2.0",
    "com.googlecode.htmlcompressor"  %  "htmlcompressor"               % "1.5.2",
    "com.google.javascript"          %  "closure-compiler"             % "r1043",
    "com.yahoo.platform.yui"         %  "yuicompressor"                % "2.4.6",
   ("org.reflections"                %  "reflections"                  % "0.9.8" notTransitive())
     .exclude("com.google.guava", "guava")
     .exclude("javassist", "javassist")
   )

  lazy val root = Project(
    id = "root",
    base = file(".")
  ).settings(
    publish := { },
    scalaVersion := "2.10.0-RC1"
  ).aggregate(templatesSbtPlugin, main)

  lazy val main = Project(
    id = "groovy-templates-plugin",
    base = file("src/templates-plugin")).settings(
      organization := "eu.delving",

      version := buildVersion,

      scalaVersion := "2.10.0-RC1",

      scalaBinaryVersion := CrossVersion.binaryScalaVersion("2.10.0-RC1"),

      resolvers += "jahia" at "http://maven.jahia.org/maven2",

      resolvers += delvingReleases,

      resolvers += delvingSnapshots,

      resolvers += Resolver.file("local-ivy-repo", file(Path.userHome + "/.ivy2/local"))(Resolver.ivyStylePatterns),

      libraryDependencies ++= dependencies,

      publishTo := Some(delvingRepository),

      credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),

      publishMavenStyle := true
    )

    lazy val templatesSbtPlugin = Project(
      id="groovy-templates-sbt-plugin",
      base=file("src/sbt-plugin")
    ).settings(
      sbtPlugin := true,

      scalaVersion := "2.9.2",
      scalaBinaryVersion  := CrossVersion.binaryScalaVersion("2.9.2"),

      organization := "eu.delving",

      version := buildVersion,

      publishTo := Some(delvingRepository),

      credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),

      publishMavenStyle := true
    )



}
