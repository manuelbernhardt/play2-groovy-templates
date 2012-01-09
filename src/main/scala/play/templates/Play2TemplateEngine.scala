package play.templates

import exceptions.TemplateNotFoundException
import play.api._
import cache.Cache
import play.templates.GroovyTemplate.ExecutableTemplate
import java.lang.{Throwable, String}
import java.io.File
import scala.collection.JavaConverters.bufferAsJavaListConverter
import collection.mutable.Buffer
import play.api.Play.current

/**
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

class Play2TemplateEngine extends TemplateEngine {

  override def startup() {
    super.startup()
    new File(precompiledTemplatesLocation).mkdir()
  }

  def initUtilsImplementation() = new Play2TemplateUtils()

  def handleException(t: Throwable) {
    t match {
      case notFound if t.isInstanceOf[TemplateNotFoundException] => PlayException("Template not found", "The template could not be found", Some(t))
      case t@_ => throw t
    }
  }

  def createTemplate(source: String) = new Play2GroovyTemplate(source)

  def createTemplate(name: String, source: String) = new Play2GroovyTemplate(name, source)

  private val precompiledTemplatesLocation = current.path + "/target/precompiled-groovy/" // this is a hack, we should use the value from SBT here but we're not in a SBT plugin at the moment

  def loadPrecompiledTemplate(name: String) = scala.io.Source.fromFile(precompiledTemplatesLocation + name).map(_.toByte).toArray

  def getPrecompiledTemplate(name: String) = new File(precompiledTemplatesLocation)

  def getTemplatePaths = Buffer(Play2VirtualFile.fromPath("app/views")).asJava

  def getCurrentResponseEncoding = "utf-8" // TODO may have to move someplace else

  def getAuthenticityToken = "" // TODO may have to move someplace else

  def handleActionInvocation(p1: String, p2: String, p3: AnyRef, p4: Boolean, p5: ExecutableTemplate) = throw new RuntimeException("not implemented")

  def reverseWithCheck(p1: String, p2: Boolean) = throw new RuntimeException("not implemented")

  def addTemplateExtensions() = new java.util.ArrayList[String]()

  def overrideTemplateSource(template: BaseTemplate, source: String) = {
    if (template.isInstanceOf[GroovyTemplate]) {
      template.source.replace("?.", "?.safeNull()?.")
    } else {
      null
    }
  }

  def getCachedTemplate(name: String, source: String): Array[Byte] = Cache.get[Array[Byte]](name).getOrElse(null)

  def cacheBytecode(byteCode: Array[Byte], name: String, source: String) {
    Cache.set(name, source)
  }

  def deleteBytecode(p1: String) { }

  def getFastTags = new java.util.ArrayList[Class[_ <: FastTags]]()

  def compileGroovyRoutes() {
    // not applicable in Play2
  }
}

case class Play2VirtualFile(name: String, relativePath: String, lastModified: java.lang.Long, exists: Boolean, isDirectory: Boolean, realFile: Option[File] = None) extends PlayVirtualFile {
  def contentAsString = null
  def getName = name
  def list() = {
    if(exists) realFile.get.listFiles().map(Play2VirtualFile.fromFile(_)).toList else List()
  }
}

object Play2VirtualFile {
  def fromFile(f: File)(implicit app: Application) = Play2VirtualFile(f.getName, f.getAbsolutePath.substring(app.path.getAbsolutePath.length()), f.lastModified(), f.exists(), f.isDirectory, Some(f))
  def fromPath(p: String)(implicit app: Application) = {
    val f = new File(app.path, p)
    Play2VirtualFile(f.getName, f.getAbsolutePath.substring(app.path.getAbsolutePath.length()), f.lastModified(), f.exists(), f.isDirectory, Some(f))
  }
}