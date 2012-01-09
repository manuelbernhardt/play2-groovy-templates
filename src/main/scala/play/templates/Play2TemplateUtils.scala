package play.templates

import play.api._
import i18n.Messages
import play.libs._
import java.lang.{Throwable, Integer, String, Class}
import play.api.Play.current
import java.io.File
import java.util.ArrayList
import scala.collection.JavaConversions.asJavaCollection
import play.cache.Cache

/**
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

class Play2TemplateUtils extends TemplateUtils {

  lazy val log = Logger("play")

  def logWarn(p1: String, p2: Object*) {
    log.warn(p1.format(p2))
  }

  def logWarn(p1: Throwable, p2: String, p3: Object*) {
    log.warn(p2.format(p3), p1)
  }

  def logError(p1: String, p2: Object*) {
    log.error(p1.format(p2))
  }

  def logError(p1: Throwable, p2: String) {
    log.error(p2, p1)
  }

  def logTraceIfEnabled(p1: String, p2: Object*) {
    if(log.isTraceEnabled) {
      log.trace(p1.format(p2))
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
    if(f.exists()) Play2VirtualFile.fromFile(f) else null
  }

  def list(parent: PlayVirtualFile) = {
    val r = new ArrayList[PlayVirtualFile]
    if(parent.exists()) {
      r.addAll(parent.asInstanceOf[Play2VirtualFile].list())
    }
    r
  }

  def encodeBASE64(p1: Array[Byte]) = ""

  def decodeBASE64(p1: String) = null

  def serialize(p1: AnyRef) = null

  def deserialize(p1: Array[Byte]) = null

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

  def getAssignableClasses(p1: Class[_]) = null

  def getAllClasses = null

  def getAbsoluteApplicationPath = current.path.getAbsolutePath
}