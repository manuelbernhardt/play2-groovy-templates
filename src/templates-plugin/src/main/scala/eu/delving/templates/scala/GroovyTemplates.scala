package eu.delving.templates.scala

import play.templates.TemplateEngineException.ExceptionType
import play.api.http.{ContentTypeOf, ContentTypes}
import play.api.Play.current
import play.api.libs.MimeTypes
import scala.collection.JavaConverters._
import play.api.mvc._
import play.templates.{TemplateEngine, TemplateEngineException}
import eu.delving.templates.exceptions.TemplateNotFoundException
import eu.delving.templates.GroovyTemplatesPlugin
import play.api.i18n.{Lang, Messages}
import com.google.common.cache.{LoadingCache, CacheLoader, CacheBuilder}
import java.util.concurrent.TimeUnit

/**
 * Helper methods for backwards-compatible behavior of Groovy templates
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

trait GroovyTemplates {
  self: Controller =>

  // if a language parameter is passed in with the parameters we use this one for language resolution
  protected val __LANG: String = "__LANG"

  protected val __AUTH_TOKEN: String = "__AUTH_TOKEN"

  protected val __RESPONSE_ENCODING: String = "__RESPONSE_ENCODING"

  /**
   * This cache holds the render arguments for the current template, from the moment an action is invoked for a given request to the moment the template
   * has been rendered. We need this mechanism in order to emulate the mutable renderArgs that exist in Play 1. The cache uses weakly referenced keys by
   * default.
   */
  private val requestRenderArgs: LoadingCache[RequestHeader, scala.collection.mutable.HashMap[String, AnyRef]] = {
    val loader = new CacheLoader[RequestHeader, scala.collection.mutable.HashMap[String, AnyRef]] {
        def load(key: RequestHeader): scala.collection.mutable.HashMap[String, AnyRef] = new scala.collection.mutable.HashMap[String, AnyRef]()
    }
    CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build(loader)
  }

  implicit def renderArgs()(implicit request: RequestHeader): scala.collection.mutable.HashMap[String, AnyRef] = requestRenderArgs.get(request)

  implicit def renderArgs(key: String)(implicit request: RequestHeader) = requestRenderArgs.get(request).get(key)

  private def className = {
    val name = this.getClass.getName
    if (name.endsWith("$")) name.substring(0, name.length() - 1) else name
  }

  implicit def contentTypeOf_HtmlTemplate(implicit codec: Codec): ContentTypeOf[GroovyTemplateContent] = {
    ContentTypeOf[GroovyTemplateContent](Some(ContentTypes.HTML))
  }

  private implicit val currentMethod: ThreadLocal[String] = new ThreadLocal[String]()

  def Template(implicit request: RequestHeader) = {
    try {
      setContext()
      renderGroovyTemplate(None, Seq())
    } finally {
      cleanup(request)
    }
  }

  def Template(args: (Symbol, Any)*)(implicit request: RequestHeader) = {
    try {
      setContext()
      renderGroovyTemplate(None, args)
    } finally {
      cleanup(request)
    }
  }

  def Template(name: String, args: (Symbol, Any)*)(implicit request: RequestHeader) = {
    try {
      setContext(false)
      renderGroovyTemplate(Some(name), args)
    } finally {
      cleanup(request)
    }
  }

  private val methodNameExtractor = """\$anonfun\$([^\$]*)(.*)""".r

  private def setContext(currentMethodLookup: Boolean = true)(implicit request: RequestHeader) {

    if (currentMethodLookup) {
      val methodCandidates = Thread.currentThread().getStackTrace.filter(_.getClassName.startsWith(this.getClass.getName + "$anonfun$"))
      val trace = methodCandidates.headOption.getOrElse(throw new TemplateEngineException(ExceptionType.UNEXPECTED, "Could not find current method in execution call", null))
      val name: Option[String] = methodNameExtractor.findFirstMatchIn(trace.getClassName.substring(this.getClass.getName.length())).map(_.group(1))
      if (name.isDefined)
        currentMethod.set(name.get)
      else
        new TemplateEngineException(ExceptionType.UNEXPECTED, "Could not figure out current method name in execution call", null)
    }

  }

  private def cleanup(request: RequestHeader) {
    requestRenderArgs.invalidate(request)
  }

  private def renderGroovyTemplate(name: Option[String], args: Seq[(Symbol, Any)])(implicit request: RequestHeader, currentMethod: ThreadLocal[String]): GroovyTemplateContent = {

    def inferTemplateName = {
      val prefix = (if (className.startsWith("controllers")) className.substring("controllers.".length) else className).replaceAll("\\.", "/") + "/" + currentMethod.get()
      val html = prefix + ".html"
      val txt = prefix + ".txt"

      if (TemplateEngine.utils.findTemplateWithPath(html).exists()) {
        html
      } else if (TemplateEngine.utils.findTemplateWithPath(txt).exists()) {
        txt
      } else {
        throw new TemplateNotFoundException("Template '%s' not found".format(html))
      }
    }

    val n: String = if (name.isEmpty) {
      inferTemplateName
    } else if (TemplateEngine.utils.findTemplateWithPath(name.get).exists()) {
      name.get
    } else {
      throw new TemplateNotFoundException("Template %s not found".format(name))
    }

    val callArgs = args.map(e => (e._1.name, e._2)).toMap
    val renderArguments = renderArgs.toMap

    val contextArgs = callArgs ++ renderArguments

    val language = if(contextArgs.contains(__LANG)) contextArgs(__LANG).toString else lang.language

    // TODO also pass in the response encoding


    val binding: Map[String, AnyRef] = Map(
      "httpRequest" -> request,
      "request" -> request, // TODO pass in the args of the session rather than the object, once it will be implemented in Play
      "session" -> request.session.data.asJava,
      "flash" -> request.flash.data.asJava,
      "params" -> request.queryString.map(m => (m._1 -> m._2.asJava)).toMap.asJava,
      "messages" -> new WrappedMessages(language),
      "lang" -> language

    )
    
//    val args = binding ++ callArgs

    val body = current.plugin[GroovyTemplatesPlugin].map(_.renderTemplate(n, renderArguments ++ callArgs ++ binding)).getOrElse(Right("")).fold(
      left => "",
      right => right
    )

    GroovyTemplateContent(body, MimeTypes.forFileName(n).getOrElse("text/html"))
  }
}

class WrappedMessages(language: String) {

  private val lang = Lang(language)

  def get(key: String) = Messages(key)(lang)

  def get(key: String, arg1: String) = Messages(key, arg1)(lang)

  def get(key: String, arg1: String, arg2: String) = Messages(key, arg1, arg2)(lang)

  def get(key: String, arg1: String, arg2: String, arg3: String) = Messages(key, arg1, arg2, arg3)(lang)

}

case class GroovyTemplateContent(body: String, contentType: String) extends Content