package play.templates

import play.api._
import play.api.mvc._
import scala.collection.JavaConversions._
/**
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

class GroovyTemplatesPlugin(app: Application) extends Plugin {

  override def enabled = true

  var engine: TemplateEngine = null

  override def onStart {
    Logger("play").info("Starting Groovy template engine")

    engine = new Play2TemplateEngine
    engine.startup()

    CustomGroovy()
  }

  override def onStop {
    Logger("play").info("Stopping Groovy template engine")
  }
  
  def renderTemplate(name: String, args: Map[String, AnyRef], request: Request[_]): String = {

    try {
      val template = GenericTemplateLoader.load(name)
      val templateArgs = args
      template.render(templateArgs)

    } catch {
      case t: Throwable =>
        engine.handleException(t)
        null
    }

  }

}