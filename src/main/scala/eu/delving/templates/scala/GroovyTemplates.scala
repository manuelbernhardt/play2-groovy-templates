package eu.delving.templates.scala

import play.templates.TemplateEngineException.ExceptionType
import play.api.http.{ContentTypeOf, ContentTypes}
import play.api.Play.current
import play.api.libs.MimeTypes
import scala.collection.JavaConverters._
import play.api.mvc._
import play.templates.{TemplateEngine, TemplateEngineException}
import eu.delving.templates.exceptions.TemplateNotFoundException
import eu.delving.templates.{GroovyTemplatesPlugin, Play2TemplateUtils}
import play.api.i18n.{Lang, Messages}

/**
 * Helper methods for backwards-compatible behavior of Groovy templates
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

trait GroovyTemplates {
  self: Controller =>

  // if a language parameter is passed in with the parameters we use this one for language resolution
  protected val __LANG: String = "__LANG"

  protected val __SESSION_ID: String = "__SESSION_ID"

  implicit def richRenderArgs(x: RenderArgs): RichRenderArgs = new RichRenderArgs(x)

  implicit def renderArgs = RenderArgs.current()

  private def className = {
    val name = getClass.getName
    if (name.endsWith("$")) name.substring(0, name.length() - 1) else name
  }

  implicit def contentTypeOf_HtmlTemplate(implicit codec: Codec): ContentTypeOf[GroovyTemplateContent] = {
    ContentTypeOf[GroovyTemplateContent](Some(ContentTypes.HTML))
  }

  private implicit val currentMethod: ThreadLocal[String] = new ThreadLocal[String]()

  def Template(implicit request: Request[_]) = {
    try {
      setContext(request)
      renderGroovyTemplate(None, Seq())
    } finally {
      cleanup()
    }
  }

  def Template(args: (Symbol, Any)*)(implicit request: Request[_]) = {
    try {
      setContext(request)
      renderGroovyTemplate(None, args)
    } finally {
      cleanup()
    }
  }

  def Template(name: String, args: (Symbol, Any)*)(implicit request: Request[_]) = {
    try {
      setContext(request)
      renderGroovyTemplate(Some(name), args)
    } finally {
      cleanup()
    }
  }

  private val methodNameExtractor = """\$anonfun\$([^\$]*)(.*)""".r

  private def setContext(implicit request: RequestHeader) {

    // request encoding
    Play2TemplateUtils.encoding.set(request.charset.getOrElse(TemplateEngine.utils.getDefaultWebEncoding))

    // current method
    val methodCandidates = Thread.currentThread().getStackTrace.filter(_.getClassName.startsWith(getClass.getName + "$anonfun$"))
    val trace = methodCandidates.headOption.getOrElse(throw new TemplateEngineException(ExceptionType.UNEXPECTED, "Could not find current method in execution call", null))
    val name: Option[String] = methodNameExtractor.findFirstMatchIn(trace.getClassName.substring(getClass.getName.length())).map(_.group(1))
    if (name.isDefined)
      currentMethod.set(name.get)
    else
      new TemplateEngineException(ExceptionType.UNEXPECTED, "Could not figure out current method name in execution call", null)

  }

  private def cleanup() {
    RenderArgs.current.set(new RenderArgs)
    Play2TemplateUtils.encoding.remove()
    Play2TemplateUtils.language.remove()
    Play2TemplateUtils.sessionId.remove()
  }

  private def renderGroovyTemplate(name: Option[String], args: Seq[(Symbol, Any)])(implicit request: Request[_], currentMethod: ThreadLocal[String]): GroovyTemplateContent = {

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

    def setLanguage(arguments: Map[String, Any]) {
      arguments.find(elem => elem._1 == __LANG).map {
        language => Play2TemplateUtils.language.set(language._2.toString)
      }.getOrElse {
        Play2TemplateUtils.language.set(lang.language)
      }
    }

    def setSessionId(arguments: Map[String, Any]) {
      arguments.find(elem => elem._1 == __SESSION_ID).map {
        sid => Play2TemplateUtils.sessionId.set(sid._2.toString)
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
    val binding: Map[String, AnyRef] = RenderArgs.current().data.asScala.toMap ++ Map(
      "request" -> request, // TODO pass in the args of the session rather than the object, once it will be implemented in Play
      "session" -> request.session.data.asJava,
      "flash" -> request.flash.data.asJava,
      "params" -> request.queryString.asJava, // TODO not sure if we shouldn't call this one "queryString" instead
      "messages" -> new WrappedMessages
    )

    setLanguage(binding ++ callArgs)
    setSessionId(binding ++ callArgs)

    val body = current.plugin[GroovyTemplatesPlugin].map(_.renderTemplate(n, binding ++ callArgs)).getOrElse(Right("")).fold(
      left => "",
      right => right
    )

    GroovyTemplateContent(body, MimeTypes.forFileName(n).getOrElse("text/html"))
  }
}

class WrappedMessages {

  private val lang = Lang(Play2TemplateUtils.language.get())

  def get(key: String) = Messages(key)(lang)

  def get(key: String, arg1: String) = Messages(key, arg1)(lang)

  def get(key: String, arg1: String, arg2: String) = Messages(key, arg1, arg2)(lang)

  def get(key: String, arg1: String, arg2: String, arg3: String) = Messages(key, arg1, arg2, arg3)(lang)

}

case class GroovyTemplateContent(body: String, contentType: String) extends Content

private[scala] class RichRenderArgs(val renderArgs: RenderArgs) {

  def +=(variable: Tuple2[String, Any]) = {
    renderArgs.put(variable._1, variable._2)
    this
  }

  def get(key: String, clazz: Class[_]) = renderArgs.get(key, clazz)

  def apply(key: String) = {
    renderArgs.data.containsKey(key) match {
      case true => Some(renderArgs.get(key))
      case false => None
    }
  }
}
