package play.templates.groovy

import play.templates.TemplateEngineException.ExceptionType
import play.api.http.{ContentTypeOf, ContentTypes}
import play.api.Play.current
import play.templates.{GroovyTemplatesPlugin, TemplateEngineException}
import play.api.libs.MimeTypes
import scala.collection.JavaConverters._
import play.api.mvc._
import play.api.i18n.Messages

/**
 * Helper methods for backwards-compatible behavior of Groovy templates
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

trait GroovyTemplates {
  self: Controller =>

  implicit def renderArgs: RichRenderArgs = new RichRenderArgs(RenderArgs.current())

  implicit def className = {
    val name = getClass.getName
    if (name.endsWith("$")) name.substring(0, name.length() - 1) else name
  }

  implicit def contentTypeOf_HtmlTemplate(implicit codec: Codec): ContentTypeOf[GroovyTemplateContent] = {
    ContentTypeOf[GroovyTemplateContent](Some(ContentTypes.HTML))
  }

  implicit val currentMethod: ThreadLocal[String] = new ThreadLocal[String]()

  def Template(implicit request: Request[_]) = {
    setCurrentMethod()
    renderGroovyTemplate(None, Seq())
  }

  def Template(args: (Symbol, AnyRef)*)(implicit request: Request[_]) = {
    setCurrentMethod()
    renderGroovyTemplate(None, args)
  }

  def Template(name: String, args: (Symbol, AnyRef)*)(implicit request: Request[_]) = {
    setCurrentMethod()
    renderGroovyTemplate(Some(name), args)
  }

  private val methodNameExtractor = """\$anonfun\$([^\$]*)(.*)""".r

  private def setCurrentMethod() {
    val methodCandidates = Thread.currentThread().getStackTrace().filter(_.getClassName.startsWith(getClass.getName + "$anonfun$"))
    val trace = methodCandidates.headOption.getOrElse(throw new TemplateEngineException(ExceptionType.UNEXPECTED, "Could not find current method in execution call", null))
    val name: Option[String] = methodNameExtractor.findFirstMatchIn(trace.getClassName.substring(getClass.getName.length())).map(_.group(1))
    if (name.isDefined)
      currentMethod.set(name.get)
    else
      new TemplateEngineException(ExceptionType.UNEXPECTED, "Could not figure out current method name in execution call", null)

  }

  private def renderGroovyTemplate(name: Option[String], args: Seq[(Symbol, AnyRef)])(implicit request: Request[_], className: String, currentMethod: ThreadLocal[String]): GroovyTemplateContent = {


    def inferTemplateName = (if (className.startsWith("controllers")) className.substring("controllers.".length) else className).replaceAll("\\.", "/") + "/" + currentMethod.get() + ".html" // TODO more types

    val n = if (name.isEmpty) inferTemplateName else name.get

    val callArgs = args.map(e => (e._1.name, e._2)).toMap
    val binding: Map[String, AnyRef] = RenderArgs.current().data.asScala.toMap ++ Map(
      "request" -> request, // TODO pass in the args of the session rather than the object, once it will be implemented in Play
      "session" -> request.session.data,
      "flash" -> request.flash.data,
      "params" -> request.queryString, // TODO not sure if we shouldn't call this one "queryString" instead
      "messages" -> new WrappedMessages
    )

    val body = current.plugin[GroovyTemplatesPlugin].map(_.renderTemplate(n, binding ++ callArgs)).getOrElse(null)

    GroovyTemplateContent(body, if (name.isDefined) MimeTypes.forFileName(name.get).getOrElse("text/html") else "text/html")
  }
}

class WrappedMessages {

  def get(key: String) = Messages(key)
  def get(key: String, args: String*) = Messages(key, args)

}

case class GroovyTemplateContent(body: String, contentType: String) extends Content

private[groovy] class RichRenderArgs(val renderArgs: RenderArgs) {

  def +=(variable: Tuple2[String, Any]) = {
    renderArgs.put(variable._1, variable._2)
    this
  }

  def apply(key: String) = {
    renderArgs.data.containsKey(key) match {
      case true => Some(renderArgs.get(key))
      case false => None
    }
  }
}
