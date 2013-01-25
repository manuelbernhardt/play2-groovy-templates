package eu.delving.templates

import _root_.java.io.File
import _root_.java.util.ArrayList
import exceptions.{TemplateCompilationException, TemplateNotFoundException}
import play.api._
import play.api.Play.current
import cache.Cache
import play.templates.GroovyTemplate.ExecutableTemplate
import collection.mutable.Buffer
import collection.JavaConverters._
import play.api.Play.current
import play.templates.TemplateEngineException.ExceptionType
import play.templates._

/**
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

class Play2TemplateEngine extends TemplateEngine {

  type TemplatesList = Any {
    def templates: Seq[String]

    def templateRoots: Seq[String]
  }

  lazy val templatesList: TemplatesList = try {
      current.classloader.loadClass("eu.delving.templates.GroovyTemplatesList$").getDeclaredField("MODULE$").get(null).asInstanceOf[TemplatesList]
    } catch {
      case t: Throwable =>
        Logger("play").error("Could not find list of templates. Did you add the groovyTemplatesList key to the sourceGenerators in your SBT build?")
      throw t
    }

  override def startup() {
    super.startup()
    val precompiled = new File(precompiledTemplatesLocation)
    org.apache.commons.io.FileUtils.deleteDirectory(precompiled)
    precompiled.mkdir()
  }

  def initUtilsImplementation() = new Play2TemplateUtils()

  def handleException(t: Throwable) {
    t match {
      case notFound if t.isInstanceOf[TemplateNotFoundException] => new PlayException("Template not found", "The template could not be found", t)
      case compilation if t.isInstanceOf[TemplateCompilationException] => {
        val e = t.asInstanceOf[TemplateCompilationException]
        if (TemplateEngine.utils.usePrecompiled()) {
          Logger("play").error("Could not compile template %s at line %s: %s".format(e.sourceName, e.line, e.getMessage))
        }
        throw e
      }
      case t@_ => throw t
    }
  }

  def createTemplate(source: String) = new Play2GroovyTemplate(source)

  def createTemplate(name: String, source: String) = new Play2GroovyTemplate(name, source)

  private val precompiledTemplatesLocation = {
    val path = current.path + "/target/precompiled-groovy/"
    new File(path).mkdirs()
    path
  }

  def loadPrecompiledTemplate(name: String) = _root_.scala.io.Source.fromFile(precompiledTemplatesLocation + name).map(_.toByte).toArray

  def getPrecompiledTemplate(name: String) = new File(precompiledTemplatesLocation + name)

  def getTemplateCompiler = new Play2GroovyTemplateCompiler

  def handleActionInvocation(controller: String, name: String, param: AnyRef, absolute: Boolean, template: ExecutableTemplate) = throw new RuntimeException("not implemented")

  def reverseWithCheck(action: String, absolute: Boolean) = throw new RuntimeException("not implemented")

  def addTemplateExtensions() = new _root_.java.util.ArrayList[String]()

  def overrideTemplateSource(template: BaseTemplate, source: String) = {
    if (template.isInstanceOf[GroovyTemplate]) {
      template.source.replace("?.", "?.safeNull()?.")
    } else {
      null
    }
  }

  def getCachedTemplate(name: String, source: String): Array[Byte] = Cache.get(name + source.hashCode()).getOrElse(return null).asInstanceOf[Array[Byte]]

  def cacheBytecode(byteCode: Array[Byte], name: String, source: String) {
    Cache.set(name + source.hashCode(), byteCode)
  }

  def deleteBytecode(name: String) {
    Cache.set(name, null)
  }

  def getFastTags = new ArrayList[Class[_ <: FastTags]]()

  def compileGroovyRoutes() {
    // not applicable in Play2
  }
}

case class Play2VirtualFile(name: String, relativePath: String, lastModified: _root_.java.lang.Long, exists: Boolean, isDirectory: Boolean, realFile: Option[File] = None) extends PlayVirtualFile {

  def contentAsString = {

    if (TemplateEngine.utils.isDevMode) {
      if (realFile.isDefined) {
        try {
          _root_.scala.io.Source.fromFile(current.getFile(relativePath)).getLines().mkString("\n")
        } catch {
          case t: Throwable =>
            TemplateEngine.utils.logError("Could not read content from file " + relativePath)
            TemplateEngine.engine.handleException(t)
            null
        }
      } else {
        throw new TemplateEngineException(ExceptionType.UNEXPECTED, "Trying to read template from non-existing file", null)
      }
    } else {
      if(!exists) {
        TemplateEngine.utils.logWarn("Trying to read content from non-existing template " + relativePath)
        throw new TemplateEngineException(ExceptionType.UNEXPECTED, "Trying to read template from non-existing resource " + relativePath, null)
      } else {
        _root_.scala.io.Source.fromInputStream(current.resourceAsStream(relativePath).get).getLines().mkString("\n")
      }
    }

  }

  def getName = name
}

object Play2VirtualFile {
  def fromFile(f: File)(implicit app: Application) =
    Play2VirtualFile(f.getName, f.getAbsolutePath.substring(app.path.getAbsolutePath.length()), f.lastModified(), f.exists(), f.isDirectory, Some(f))

  def fromPath(p: String)(implicit app: Application) = {
    if(TemplateEngine.utils.isDevMode) {
      val f = app.getFile(p)
      Play2VirtualFile(p.split(File.separator).toList.reverse.head, p, f.lastModified(), f.exists(), f.isDirectory, Some(f))
    } else {
      app.resource(p) match {
        case Some(r) =>
          Play2VirtualFile(p.split("/").reverse.toList.head, p, System.currentTimeMillis(), true, false)
        case None =>
          Play2VirtualFile(p.split("/").reverse.toList.head, p, System.currentTimeMillis(), false, false)
      }
    }
  }

}