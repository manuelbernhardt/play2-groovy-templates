package play.templates

import play.api._
import i18n.Messages
import play.libs._
import java.lang.{Throwable, Integer, String, Class}
import play.api.Play.current
import scala.collection.JavaConversions.asJavaCollection
import play.cache.Cache
import java.util.ArrayList
import java.io._
import org.reflections.scanners.SubTypesScanner

/**
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

class Play2TemplateUtils extends TemplateUtils {

  lazy val log = Logger("play")

  def logWarn(p1: String, p2: Object*) {
    log.warn(p1.format(p2 : _ *))
  }

  def logWarn(p1: Throwable, p2: String, p3: Object*) {
    log.warn(p2.format(p3 : _ *), p1)
  }

  def logError(p1: String, p2: Object*) {
    log.error(p1.format(p2 : _ *))
  }

  def logError(p1: Throwable, p2: String) {
    log.error(p2, p1)
  }

  def logTraceIfEnabled(p1: String, p2: Object*) {
    if (log.isTraceEnabled) {
      log.trace(p1.format(p2 : _ *))
    }
  }

  def isDevMode = Play.isDev

  def usePrecompiled() = false

  // TODO
  def getDefaultWebEncoding = "utf-8"

  def findTemplateWithPath(path: String) = {
    // TODO add more roots
    Play2VirtualFile.fromPath("/app/views/" + path)
  }

  def findFileWithPath(path: String) = {
    // TODO add more roots
    val f = new File(current.path, path)
    if (f.exists()) Play2VirtualFile.fromFile(f) else null
  }

  def list(parent: PlayVirtualFile) = {
    val r = new ArrayList[PlayVirtualFile]
    if (parent.exists()) {
      r.addAll(parent.asInstanceOf[Play2VirtualFile].list())
    }
    r
  }

  def encodeBASE64(p1: Array[Byte]) = new sun.misc.BASE64Encoder().encode(p1)

  def decodeBASE64(p1: String) = new sun.misc.BASE64Decoder().decodeBuffer(p1)

  def serialize(o: AnyRef): Array[Byte] = {
    val baos: ByteArrayOutputStream = new ByteArrayOutputStream
    val oo: ObjectOutputStream = new ObjectOutputStream(baos)
    oo.writeObject(o)
    oo.flush()
    oo.close()
    baos.toByteArray
  }

  def deserialize(b: Array[Byte]): AnyRef = {
    val bais: ByteArrayInputStream = new ByteArrayInputStream(b)
    val oi: ObjectInputStream = new ObjectInputStream(bais)
    oi.readObject
  }


  // TODO
  def getLang = "en"

  def getMessage(key: Any, args: Object*) = Messages(key.toString, args)

  def getDateFormat = ""

  def getCurrencySymbol(p1: String) = ""

  def htmlEscape(text: String) = org.apache.commons.lang.StringEscapeUtils.escapeHtml(text)

  def parseDuration(duration: String) = Time.parseDuration(duration)

  def getMessages = Messages

  def getPlay = Play

  def getCached(key: String) = Cache.get(key)

  def setCached(key: String, value: String, expiration: Integer) {
    Cache.set(key, value, expiration)
  }

  def getClassLoader = current.classloader

  def getAssignableClasses(clazz: Class[_]) = {
    import org.reflections._
    val assignableClasses = new Reflections(new util.ConfigurationBuilder()
      .addUrls(util.ClasspathHelper.forPackage("controllers", current.classloader))
      .setScanners(new scanners.SubTypesScanner)
    ).getSubTypesOf(clazz)
    val list = new ArrayList[Class[_]]()
    list.addAll(assignableClasses)
    list
  }

  def getAllClasses = {
    import org.reflections._
    val assignableClasses = new Reflections(new util.ConfigurationBuilder()
      .addUrls(util.ClasspathHelper.forPackage("controllers", current.classloader))
      .setScanners(new scanners.SubTypesScanner)
    ).getSubTypesOf(classOf[Object])
    val list = new ArrayList[Class[_]]()
    list.addAll(assignableClasses)
    list

  }

  def getAbsoluteApplicationPath = current.path.getAbsolutePath
}