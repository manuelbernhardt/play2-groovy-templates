import sbt._
import Keys._

object PluginBuild extends Build {

  val buildVersion = "1.6.0"


  val delvingReleases = "Delving Releases Repository" at "http://nexus.delving.org/nexus/content/repositories/releases"
  val delvingSnapshots = "Delving Snapshot Repository" at "http://nexus.delving.org/nexus/content/repositories/snapshots"
  val delvingRepository = if(buildVersion.endsWith("SNAPSHOT")) delvingSnapshots else delvingReleases

  val dependencies = Seq(
    "play"                           %% "play"                         % "2.1.0",
    "play"                           %% "play-java"                    % "2.1.0",
    "play"                           %% "templates"                    % "2.1.0",
    "eu.delving"                     %  "groovy-templates-engine"      % "0.7.5",
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
    scalaVersion := "2.10.0"
  ).aggregate(templatesSbtPlugin, main)

  lazy val main = Project(
    id = "groovy-templates-plugin",
    base = file("src/templates-plugin")).settings(
      organization := "eu.delving",

      version := buildVersion,

      scalaVersion := "2.10.0",

      scalaBinaryVersion := CrossVersion.binaryScalaVersion("2.10.0"),

      resolvers += delvingReleases,

      resolvers += delvingSnapshots,
      
      resolvers += "Typesafe releases" at "http://repo.typesafe.com/typesafe/releases/",
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

      organization := "eu.delving",

      version := buildVersion,

      scalaVersion := "2.10.0",
     
      scalaBinaryVersion := CrossVersion.binaryScalaVersion("2.10.0"),

      publishTo := Some(delvingRepository),

      credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),

      publishMavenStyle := true
    )



}
