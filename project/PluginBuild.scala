import sbt._
import Keys._

object PluginBuild extends Build {

  val buildVersion = "1.6.4-SNAPSHOT"

  val releases = "Sonatype OSS Releases Repository" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
  val snapshots = "Sonatype OSS Snapshot Repository" at "https://oss.sonatype.org/content/repositories/snapshots"
  val publicationRepository = if(buildVersion.endsWith("SNAPSHOT")) snapshots else releases

  val dependencies = Seq(
    "com.typesafe.play"                           %% "play"                         % "2.2.2",
    "com.typesafe.play"                           %% "play-java"                    % "2.2.2",
    "com.typesafe.play"                           %% "templates"                    % "2.2.2",
    "com.typesafe.play"                           %% "play-cache"                   % "2.2.2",
    "eu.delving"                                  %  "groovy-templates-engine"      % "0.7.5",
    "commons-io"                                  %  "commons-io"                   % "2.0",
    "com.googlecode.htmlcompressor"               %  "htmlcompressor"               % "1.5.2",
    "com.google.javascript"                       %  "closure-compiler"             % "r1043",
    "com.yahoo.platform.yui"                      %  "yuicompressor"                % "2.4.6",
   ("org.reflections"                             %  "reflections"                  % "0.9.8" notTransitive())
     .exclude("com.google.guava", "guava")
     .exclude("javassist", "javassist")
   )

  lazy val root = Project(
    id = "root",
    base = file(".")
  ).settings(
    publish := { },
    scalaVersion := "2.10.2",
    scalaBinaryVersion := CrossVersion.binaryScalaVersion("2.10.2")
  ).aggregate(templatesSbtPlugin, main)

  lazy val main = Project(
    id = "groovy-templates-plugin",
    base = file("src/templates-plugin")).settings(
      organization := "io.bernhardt",

      version := buildVersion,

      scalaVersion := "2.10.2",

      scalaBinaryVersion := CrossVersion.binaryScalaVersion("2.10.2"),

      resolvers += "Typesafe releases" at "http://repo.typesafe.com/typesafe/releases/",

      resolvers += Resolver.file("local-ivy-repo", file(Path.userHome + "/.ivy2/local"))(Resolver.ivyStylePatterns),

      libraryDependencies ++= dependencies,

      publishTo := Some(publicationRepository),

      credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),

      publishMavenStyle := true
    )

    lazy val templatesSbtPlugin = Project(
      id="groovy-templates-sbt-plugin",
      base=file("src/sbt-plugin")
    ).settings(
      sbtPlugin := true,

      organization := "io.bernhardt",

      version := buildVersion,

      publishTo := Some(publicationRepository),

      credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),

      publishMavenStyle := true
    )



}
