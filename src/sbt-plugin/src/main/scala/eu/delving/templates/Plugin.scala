package eu.delving.templates

import sbt._
import sbt.NameFilter._
import Keys._

/**
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

object Plugin extends sbt.Plugin {

  lazy val groovyTemplatesList = TaskKey[Seq[File]]("groovyTemplatesList")

  case class TemplatePaths(sourceDir: File, targetDir: File) {

    val rootTemplatePaths = {
      val modules = sourceDir / "modules"
      val moduleRoots: List[String] = if (modules.isDirectory) {
        modules.listFiles.filter(_.isDirectory).map("/modules/" + _.getName + "/app/views/").toList
      } else {
        List.empty
      }
      Seq("/app/views/") ++ moduleRoots
    }
    val templateNames = rootTemplatePaths.map {
      root => {
        val htmlTemplates = ((sourceDir / root) ** "*.html").get.filterNot(_.getName.endsWith("scala.html"))
        val txtTemplates = ((sourceDir / root) ** "*.txt").get.filterNot(_.getName.endsWith("scala.txt"))
        (htmlTemplates ++ txtTemplates).map(_.getAbsolutePath.substring((sourceDir / root).getAbsolutePath.length + 1))
      }
    }.flatten

    val templates = rootTemplatePaths.map {
      root =>
        (((sourceDir / root) ** "*.html").get ++ ((sourceDir / root) ** "*.txt").get).
          filterNot(_.getName.contains(".scala."))
    }.flatten


    def templateList: File = {
      val f = targetDir / "GroovyTemplatesList.scala"
      val templatesListCode =
        """package eu.delving.templates
          |
          |object GroovyTemplatesList extends TemplatesList {
          |  def templates = Seq("%s")
          |}
        """.stripMargin.format(templateNames.mkString("""", """"))
      IO.write(f, templatesListCode)
      f
    }

  }

  lazy val groovyTemplatesSettings: Seq[Project.Setting[_]] = Seq(
    groovyTemplatesList <<= (baseDirectory, sourceManaged in Compile) map {
      (source, target) => Seq(TemplatePaths(source, target).templateList)
    },
    copyTemplates <<= copyTemplatesTask,
    copyResources in Compile <<= (copyResources in Compile, copyTemplates) map { (r, pr) => r ++ pr }
  )

  val copyTemplates = TaskKey[Seq[(File, File)]]("copy-templates")
  def copyTemplatesTask =
	(baseDirectory, classDirectory in Compile, cacheDirectory, resources in Compile, resourceDirectories in Compile, streams) map { (source, target, cache, resrcs, dirs, s) =>
		val cacheFile = cache / "copy-templates"
		val mappings: Seq[(File, File)] = TemplatePaths(source, target).templates.map(t => (t -> new java.io.File(target, "templates/" + (t.getAbsolutePath.substring(source.getAbsolutePath.length + 1).split("app/views/").reverse.head))))
		Sync(cacheFile)( mappings )
		mappings
	}


}
