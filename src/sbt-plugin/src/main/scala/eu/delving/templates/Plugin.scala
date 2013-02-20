package eu.delving.templates

import sbt._
import sbt.NameFilter._
import Keys._
import sbt.Load.BuildStructure

/**
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

object Plugin extends sbt.Plugin {

  lazy val groovyTemplatesList = TaskKey[Seq[File]]("groovyTemplatesList")

  case class TemplatePaths(baseDir: File, projectRef: ProjectRef, buildStructure: BuildStructure, targetDir: File) {

    val rootTemplatePaths = {

      val dependencies = aggregateBaseDirs(projectRef, buildStructure)
      val dependencyTemplateRoots: Seq[String] = dependencies.flatMap { d =>
        if (d.relativeTo(baseDir).isDefined) {
          d.relativeTo(baseDir).map(_.getPath + "/app/views/")
        } else {
          // build it in the other direction, assuming they have a common length
          // this is done in Apache FileUtils but we are in a SBT plugin, let's keep deps to a minimum
          val common = baseDir.getAbsolutePath.zip(d.getAbsolutePath).takeWhile(Function.tupled(_ == _)).map(_._1).mkString
          if (!common.isEmpty) {
            // backslash is a special character in regular expressions and must be escaped on windows platforms
            val fileSeparator = _root_.java.io.File.separator.replaceAll("""\\""", """\\\\""")
            val levels: Int = baseDir.getAbsolutePath.substring(common.length).split(fileSeparator).length
            val sep = (for(i <- 0 until levels) yield ".." + fileSeparator).mkString
            Some(sep + d.getAbsolutePath.substring(common.length) + "/app/views/")
          } else {
            None
          }
        }
      }

      Seq("/app/views/") ++ dependencyTemplateRoots
    }

    val templateNames = rootTemplatePaths.map {
      root => {
        val htmlTemplates = ((baseDir / root) ** "*.html").get.filterNot(_.getName.endsWith("scala.html"))
        val txtTemplates = ((baseDir / root) ** "*.txt").get.filterNot(_.getName.endsWith("scala.txt"))
        (htmlTemplates ++ txtTemplates).map(_.getAbsolutePath.substring((baseDir / root).getAbsolutePath.length + 1))
      }
    }.flatten

    val templates = rootTemplatePaths.map {
      root =>
        (((baseDir / root) ** "*.html").get ++ ((baseDir / root) ** "*.txt").get).
          filterNot(_.getName.contains(".scala."))
    }.flatten

    def aggregateBaseDirs(proj: ProjectRef, struct: Load.BuildStructure) = {
      val deps = collectionProjectDependencies(_.dependencies.map(_.project))(proj, struct)
      val baseDirs: Seq[File] = deps.flatMap(ref => (baseDirectory in (ref, Compile)).get(struct.data))
      baseDirs
    }

    def collectionProjectDependencies(op: ResolvedProject => Seq[ProjectRef])(projRef: ProjectRef, struct: Load.BuildStructure): Seq[ProjectRef] = {
      val deps = Project.getProject(projRef, struct).toSeq.flatMap(op)
      deps.flatMap(ref => ref +: collectionProjectDependencies(op)(ref, struct)).distinct
    }

    def toPathList(s: Seq[String]) = s.map(path => path.replaceAllLiterally("""\""", """\\""")).mkString("""", """")


    def templateList: File = {
      val f = targetDir / "GroovyTemplatesList.scala"
      val templatesListCode =
        """package eu.delving.templates
          |
          |object GroovyTemplatesList extends TemplatesList {
          |  def templates = Seq("%s")
          |  def templateRoots = Seq("%s")
          |}
        """.stripMargin.format(
          toPathList(templateNames),
          toPathList(rootTemplatePaths)
        )
      IO.write(f, templatesListCode)
      f
    }

  }

  lazy val groovyTemplatesSettings: Seq[Project.Setting[_]] = Seq(
    groovyTemplatesList <<= (baseDirectory, thisProjectRef, buildStructure, sourceManaged in Compile) map {
      (base, projectRef, buildStructure, target) => Seq(TemplatePaths(base, projectRef, buildStructure, target).templateList)
    },
    copyTemplates <<= copyTemplatesTask,
    copyResources in Compile <<= (copyResources in Compile, copyTemplates) map { (r, pr) => r ++ pr }
  )

  val copyTemplates = TaskKey[Seq[(File, File)]]("copy-templates")
  def copyTemplatesTask =
	(baseDirectory,  thisProjectRef, buildStructure, classDirectory in Compile, cacheDirectory, resources in Compile, resourceDirectories in Compile, streams) map { (base, projectRef, buildStructure, target, cache, resrcs, dirs, s) =>
		val cacheFile = cache / "copy-templates"
		val mappings: Seq[(File, File)] = TemplatePaths(base, projectRef, buildStructure, target).templates.map(t => (t -> new java.io.File(target, "templates/" + (t.getAbsolutePath.substring(base.getAbsolutePath.length + 1).split("app/views/").reverse.head))))
		Sync(cacheFile)( mappings )
		mappings
	}


}
