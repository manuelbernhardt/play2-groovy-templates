package play.templates.groovy

import play.api.libs.MimeTypes
import play.api.Play.current
import play.templates.GroovyTemplatesPlugin
import play.api.mvc.{Action, Request, Content}

/**
 * 
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

case class GroovyTemplateContent(name: Option[String], args: Seq[(Symbol, AnyRef)])(implicit request: Request[_], className: String, currentMethod: ThreadLocal[String]) extends Content {

  def body = {
    val n = if (name.isEmpty) inferTemplateName else name.get
    current.plugin[GroovyTemplatesPlugin].map(_.renderTemplate(n, args.map(e => (e._1.name, e._2)).toMap, request)).getOrElse(null)
  }

  def contentType = if(name.isDefined) MimeTypes.forFileName(name.get).getOrElse("text/html") else "text/html"

  def inferTemplateName = (if(className.startsWith("controllers")) className.substring("controllers.".length) else className).replaceAll("\\.", "/") + "/" + methodName + ".html" // TODO more types

  def methodName = currentMethod.get()
}