package play.templates.groovy

import play.api.libs.MimeTypes
import play.api.Play.current
import play.templates.GroovyTemplatesPlugin
import play.api.mvc.{Request, Content}

/**
 * 
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

case class GroovyTemplateContent(name: Option[String], args: Seq[(Symbol, AnyRef)])(implicit request: Request[_], className: String, currentMethod: ThreadLocal[String]) extends Content {

  def body = {
    val n = if (name.isEmpty) inferTemplateName else name.get

    val callArgs = args.map(e => (e._1.name, e._2)).toMap
    // TODO equivalent of renderArgs?
    val binding = Map(
      "request" -> request,
      "session" -> request.session,
      "flash" -> request.flash,
      "params" -> request.queryString // TODO not sure if we shouldn't call this one "queryString" instead
    )

    current.plugin[GroovyTemplatesPlugin].map(_.renderTemplate(n, callArgs ++ binding)).getOrElse(null)
  }

  def contentType = if(name.isDefined) MimeTypes.forFileName(name.get).getOrElse("text/html") else "text/html"

  def inferTemplateName = (if(className.startsWith("controllers")) className.substring("controllers.".length) else className).replaceAll("\\.", "/") + "/" + methodName + ".html" // TODO more types

  def methodName = currentMethod.get()
}