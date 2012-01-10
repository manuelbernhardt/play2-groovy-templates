package play.templates.groovy

import play.api.http.ContentTypes
import play.api.mvc.{ContentTypeOf, Request, Controller}
import play.api.mvc.Codec
import play.templates.TemplateEngineException
import play.templates.TemplateEngineException.ExceptionType

/**
 * Helper methods for backwards-compatible behavior of Groovy templates
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

trait GroovyTemplates {
  self: Controller =>

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
    GroovyTemplateContent(None, Seq())
  }

  def Template(args: (Symbol, AnyRef)*)(implicit request: Request[_]) = {
    setCurrentMethod()
    GroovyTemplateContent(None, args)
  }

  def Template(name: String, args: (Symbol, AnyRef)*)(implicit request: Request[_]) = {
    setCurrentMethod()
    GroovyTemplateContent(Some(name), args)
  }

  private val methodNameExtractor = """\$anonfun\$([^\$]*)(.*)""".r

  private def setCurrentMethod() {
    val methodCandidates = Thread.currentThread().getStackTrace().filter(_.getClassName.startsWith(getClass.getName + "$anonfun$"))
    val trace = methodCandidates.headOption.getOrElse(throw new TemplateEngineException(ExceptionType.UNEXPECTED, "Could not find current method in execution call", null))
    val name: Option[String] = methodNameExtractor.findFirstMatchIn(trace.getClassName.substring(getClass.getName.length())).map(_.group(1))
    if(name.isDefined)
      currentMethod.set(name.get)
    else
      new TemplateEngineException(ExceptionType.UNEXPECTED, "Could not figure out current method name in execution call", null)

  }
}
