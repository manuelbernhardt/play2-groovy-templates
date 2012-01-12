package play.templates

import play.api._
import org.reflections._
import scala.collection.JavaConversions._
import org.reflections.Reflections

/**
 * Plugin for rendering Groovy templates
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

class GroovyTemplatesPlugin(app: Application) extends Plugin {

  override def enabled = true

  var engine: TemplateEngine = null

  var allClassesMetadata: Reflections = null


  override def onStart {
    engine = new Play2TemplateEngine
    engine.startup()

    // cache lookup off all classes
    // the template engine needs this to allow static access to classes with "nice" names (without the $'s)
    allClassesMetadata = new Reflections(new util.ConfigurationBuilder()
      .addUrls(util.ClasspathHelper.forJavaClassPath())
      .setScanners(new scanners.SubTypesScanner))
    CustomGroovy()

    Logger("play").info("Groovy template engine started")
  }

  override def onStop {
    Logger("play").info("Stopping Groovy template engine")
  }

  def renderTemplate(name: String, args: Map[String, AnyRef]): String = {

    try {
      Logger("play").info("Loading template " + name)
      val template = GenericTemplateLoader.load(name)
      Logger("play").info("Starting to render")
      val templateArgs = args
      template.render(templateArgs)

    } catch {
      case t: Throwable =>
        engine.handleException(t)
        null
    }

  }

}