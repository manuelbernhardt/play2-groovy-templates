package eu.delving.templates

import _root_.java.io.File
import _root_.java.util.ArrayList
import exceptions.{TemplateNotFoundException}
import play.api._
import cache.Cache
import play.templates.GroovyTemplate.ExecutableTemplate
import _root_.scala.collection.JavaConverters.bufferAsJavaListConverter
import collection.mutable.Buffer
import play.api.Play.current
import play.templates.TemplateEngineException.ExceptionType
import play.templates._

/**
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

class Play2TemplateEngine extends TemplateEngine {

  override def startup() {
    super.startup()
    val precompiled = new File(precompiledTemplatesLocation)
    org.apache.commons.io.FileUtils.deleteDirectory(precompiled)
    precompiled.mkdir()
  }

  def initUtilsImplementation() = new Play2TemplateUtils()

  def handleException(t: Throwable) {
    t match {
      case notFound if t.isInstanceOf[TemplateNotFoundException] => PlayException("Template not found", "The template could not be found", Some(t))
      case compilation if t.isInstanceOf[play.templates.TemplateCompilationError] => {
        val e = t.asInstanceOf[play.templates.TemplateCompilationError]
        if(TemplateEngine.utils.usePrecompiled()) {
          Logger("play").error("Could not compile template %s at line %s: %s".format(e.source.getName, e.line, e.getMessage))
        }
        throw new eu.delving.templates.exceptions.TemplateCompilationException(e.getMessage, Some(e.line), None, Some(e.source), Some(e.source.getName), None)
      }
      case t@_ => throw t
    }
  }

  def createTemplate(source: String) = new Play2GroovyTemplate(source)

  def createTemplate(name: String, source: String) = new Play2GroovyTemplate(name, source)

  private val precompiledTemplatesLocation = current.path + "/target/precompiled-groovy/" // this is a hack, we should use the value from SBT here but we're not in a SBT plugin at the moment

  def loadPrecompiledTemplate(name: String) = _root_.scala.io.Source.fromFile(precompiledTemplatesLocation + name).map(_.toByte).toArray

  def getPrecompiledTemplate(name: String) = new File(precompiledTemplatesLocation + name)

  def getTemplateCompiler = new Play2GroovyTemplateCompiler

  def getTemplatePaths = Buffer(Play2VirtualFile.fromPath("app/views")).asJava

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
  def contentAsString = if (realFile.isDefined) {
    try {
      _root_.scala.io.Source.fromFile(current.getFile(relativePath)).getLines().mkString("\n")
    } catch {
      case t =>
        TemplateEngine.utils.logError("Could not read content from file " + relativePath)
        TemplateEngine.engine.handleException(t)
        null
    }
  } else {
    throw new TemplateEngineException(ExceptionType.UNEXPECTED, "Trying to read template from non-existing file", null)
  }

  def getName = name

  def list() = {
    if (exists) realFile.get.listFiles().map(Play2VirtualFile.fromFile(_)).toList else List()
  }
}

object Play2VirtualFile {
  def fromFile(f: File)(implicit app: Application) = Play2VirtualFile(f.getName, f.getAbsolutePath.substring(app.path.getAbsolutePath.length()), f.lastModified(), f.exists(), f.isDirectory, Some(f))

  def fromPath(p: String)(implicit app: Application) = {
    val f = app.getFile(p)
    Play2VirtualFile(p.split(File.separator).reverse.head, p, f.lastModified(), f.exists(), f.isDirectory, Some(f))
  }

}