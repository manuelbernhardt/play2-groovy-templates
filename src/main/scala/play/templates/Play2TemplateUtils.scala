package play.templates

import groovy.WrappedMessages
import play.api._
import i18n.{Lang, Messages}
import play.libs._
import java.lang.{Throwable, Integer, String, Class}
import play.api.Play.current
import scala.collection.JavaConversions.asJavaCollection
import play.cache.Cache
import java.io._
import java.util.ArrayList

class Play2TemplateUtils extends TemplateUtils {

  lazy val rootTemplatePaths = {
    val modules = Play2VirtualFile.fromPath("/modules")
    val moduleRoots: List[String] = if(modules.isDirectory) modules.realFile.get.listFiles().filter(_.isDirectory).map(_.getName + "/app/views/").toList else List.empty
    Seq("/app/views/") ++ moduleRoots
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
    for (p <- rootTemplatePaths) {
      val t = Play2VirtualFile.fromPath(p + path)
      if(t.exists) return t
    }
    Play2VirtualFile.fromPath("/app/views/" + path)
  }

  def findFileWithPath(path: String): Play2VirtualFile = {
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

  def getDateFormat = throw new RuntimeException("Not implemented")

  def getCurrencySymbol(p1: String) = throw new RuntimeException("Not implemented")

  def htmlEscape(text: String) = org.apache.commons.lang.StringEscapeUtils.escapeHtml(text)

  def parseDuration(duration: String) = Time.parseDuration(duration)

  def getMessages = new WrappedMessages

  def getPlay = Play

  def getCached(key: String) = Cache.get(key)

  def setCached(key: String, value: String, expiration: Integer) {
    Cache.set(key, value, expiration)
  }

  def getClassLoader = current.classloader

  def getAssignableClasses(clazz: Class[_]) = current.plugin[GroovyTemplatesPlugin].map(_.getAssignableClasses(clazz)).getOrElse(new ArrayList[Class[_]])

  def getAllClasses = current.plugin[GroovyTemplatesPlugin].map(_.getAllClasses).getOrElse(new ArrayList[Class[_]])

  def getAbsoluteApplicationPath = current.path.getAbsolutePath


  def getDefaultWebEncoding = "utf-8" // TODO read from config file

  // per-request things, need a better place

  def getCurrentResponseEncoding = Play2TemplateUtils.encoding.get

  def getAuthenticityToken = "" // TODO implement. May have to move someplace else

  def getLang = Play2TemplateUtils.language.get()

  def getMessage(key: Any, args: Object*) = Messages(key.toString, args : _ *)(Lang(getLang))



}

object Play2TemplateUtils {

  // per-request values. due to how the engine was ported from Play 1 it's the easiest for now to go with ThreadLocal-s
  
  val language = new ThreadLocal[String] {
    override def initialValue() = Lang.defaultLang.language
  }
  
  val encoding = new ThreadLocal[String] {
    override def initialValue() = "utf-8" // TODO take from config file
  }
  
}