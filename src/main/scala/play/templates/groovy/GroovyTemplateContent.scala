package play.templates.groovy

import play.api.libs.MimeTypes
import play.api.Play.current
import play.templates.GroovyTemplatesPlugin
import play.api.mvc.{Request, Content}
import scala.collection.JavaConverters._
/**
 * 
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

case class GroovyTemplateContent(name: Option[String], args: Seq[(Symbol, AnyRef)])(implicit request: Request[_], className: String, currentMethod: ThreadLocal[String]) extends Content {

  def body = {
    val n = if (name.isEmpty) inferTemplateName else name.get

    val callArgs = args.map(e => (e._1.name, e._2)).toMap
    val binding: Map[String, AnyRef] = RenderArgs.current().data.asScala.toMap ++ Map(
      "request" -> request, // TODO pass in the args of the session rather than the object, once it will be implemented in Play
      "session" -> request.session.data,
      "flash" -> request.flash.data,
      "params" -> request.queryString // TODO not sure if we shouldn't call this one "queryString" instead
    )

    current.plugin[GroovyTemplatesPlugin].map(_.renderTemplate(n, binding ++ callArgs)).getOrElse(null)
  }

  def contentType = if(name.isDefined) MimeTypes.forFileName(name.get).getOrElse("text/html") else "text/html"

  def inferTemplateName = (if(className.startsWith("controllers")) className.substring("controllers.".length) else className).replaceAll("\\.", "/") + "/" + methodName + ".html" // TODO more types

  def methodName = currentMethod.get()
}