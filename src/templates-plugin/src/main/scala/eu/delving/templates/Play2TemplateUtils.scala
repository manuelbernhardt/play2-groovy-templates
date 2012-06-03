package eu.delving.templates

import eu.delving.templates.scala.WrappedMessages
import play.api._
import i18n.{Lang, Messages}
import play.libs._
import _root_.java.lang.{Throwable, Integer, String, Class}
import play.api.Play.current
import _root_.scala.collection.JavaConversions.asJavaCollection
import play.cache.Cache
import _root_.java.io._
import _root_.java.util.ArrayList
import play.templates.{PlayVirtualFile, TemplateUtils}

class Play2TemplateUtils extends TemplateUtils {

  lazy val rootTemplatePaths = {
    val modules = Play2VirtualFile.fromPath("/modules")
    val moduleRoots: List[String] = if(modules.isDirectory) {
      modules.realFile.get.listFiles().filter(_.isDirectory).map("/modules/" + _.getName + "/app/views/").toList
    } else {
      List.empty
    }
    Seq("/app/views/") ++ moduleRoots ++ Seq("/views")
  }

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

  def usePrecompiled() = Play.isProd

  def findTemplateWithPath(path: String): Play2VirtualFile = {
    if(isDevMode) {
      for (p <- rootTemplatePaths) {
        val t = Play2VirtualFile.fromPath(p + path)
        if(t.exists) return t
      }
      Play2VirtualFile.fromPath("/app/views/" + path)
    } else {
      Play2VirtualFile.fromPath("templates/" + (if(path.startsWith("/")) path.drop(1) else path))
    }
  }

  def findFileWithPath(path: String): Play2VirtualFile = {
    // TODO add more roots
    val f = new File(current.path, path)
    if (f.exists()) Play2VirtualFile.fromFile(f) else null
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

  def getDateFormat = throw new RuntimeException("Not implemented")

  def getCurrencySymbol(p1: String) = throw new RuntimeException("Not implemented")

  def htmlEscape(text: String) = org.apache.commons.lang.StringEscapeUtils.escapeHtml(text)

  def parseDuration(duration: String) = Time.parseDuration(duration)

  def getPlay = Play

  def getMessages(language: String): AnyRef = new WrappedMessages(language)

  def getCached(key: String) = Cache.get(key)

  def setCached(key: String, value: String, expiration: Integer) {
    Cache.set(key, value, expiration)
  }

  def getClassLoader = current.classloader

  def getAssignableClasses(clazz: Class[_]) = current.plugin[GroovyTemplatesPlugin].map(_.getAssignableClasses(clazz)).getOrElse(new ArrayList[Class[_]])

  def getAllClasses = current.plugin[GroovyTemplatesPlugin].map(_.getAllClasses).getOrElse(new ArrayList[Class[_]])

  def getAbsoluteApplicationPath = current.path.getAbsolutePath

  def getDefaultWebEncoding = "utf-8" // TODO read from config file

  def getMessage(language: String, key: Any, args: AnyRef*): String = Messages(key.toString, args : _ *)(Lang(language))

}