package eu.delving.templates

import _root_.java.util.ArrayList
import _root_.java.util.concurrent.ConcurrentHashMap
import play.api._
import play.api.Play.current
import org.reflections._
import _root_.scala.collection.JavaConverters._
import collection.mutable.HashMap
import play.templates.{GenericTemplateLoader, TemplateEngine}
import com.googlecode.htmlcompressor.compressor.HtmlCompressor
import play.templates.exceptions.TemplateCompilationException

/**
 * Plugin for rendering Groovy templates
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

class GroovyTemplatesPlugin(app: Application) extends Plugin {

  override def enabled = app.configuration.getBoolean("play.groovyTemplates.enabled").getOrElse(true)

  val compressor = new HtmlCompressor()
  compressor.setRemoveComments(false)

  var engine: TemplateEngine = null

  var allClassesMetadata: Reflections = null

  var assignableClassesCache = new HashMap[Class[_], ArrayList[Class[_]]]

  var allClassesCache = new ArrayList[Class[_]]

  override def onStart {
    engine = new Play2TemplateEngine
    engine.startup()

    // cache lookup of classes
    // the template engine needs this to allow static access to classes with "nice" names (without the $'s)
    // we also use this to find FastTag-s and JavaExtension-s, assuming they live in "views"
    allClassesMetadata = new Reflections(new util.ConfigurationBuilder()
      .addUrls(util.ClasspathHelper.forPackage("play.templates", TemplateEngine.utils.getClassLoader))
      .addUrls(util.ClasspathHelper.forPackage("views", TemplateEngine.utils.getClassLoader))
      .setScanners(new AllTypesScanner, new scanners.SubTypesScanner))

    Logger("play").debug("Found %s classes".format(allClassesMetadata.getStore.getKeysCount))

    if(TemplateEngine.utils.usePrecompiled()) {
      Logger("play").info("Precompiling...")

      try {
        engine.asInstanceOf[Play2TemplateEngine].templatesList.templates.map {
          template => {
            val loaded = GenericTemplateLoader.load(template)
            if (loaded != null) {
              try {
                  loaded.compile();
              } catch {
                case tce: TemplateCompilationException => {
                  TemplateEngine.utils.logError("Template %s does not compile at line %d", tce.getTemplate().name, tce.getLineNumber());
                  throw tce
                }
              }
            }
          }
        }
      } catch {
        case t: Throwable => TemplateEngine.engine.handleException(t)
      }
    }

    CustomGroovy()

    Logger("play").info("Groovy template engine started")
  }

  override def onStop {
    Logger("play").info("Stopping Groovy template engine")
  }

  def getAssignableClasses(clazz: Class[_]) = {
    if (assignableClassesCache.contains(clazz)) {
      assignableClassesCache(clazz)
    } else {
      val assignableClasses = allClassesMetadata.getSubTypesOf(clazz)
      val list = new ArrayList[Class[_]]()
      list.addAll(assignableClasses)
      assignableClassesCache.put(clazz, list)
      list
    }
  }

  def renderTemplate(name: String, args: Map[String, Any]): Either[Throwable, String] = {

    try {
      val n = System.currentTimeMillis()
      Logger("play").debug("Loading template " + name)
      val template = GenericTemplateLoader.load(name)
      Logger("play").debug("Starting to render")
      val templateArgs = new ConcurrentHashMap[String, AnyRef](args.map(e => (e._1, e._2.asInstanceOf[AnyRef])).asJava)
      val res = template.render(templateArgs)
      Logger("play").debug("Rendered template %s in %s".format(name, System.currentTimeMillis() - n))
      val result = if(Play.isProd && app.configuration.getBoolean("play.groovyTemplates.htmlCompression").getOrElse(true) && name.endsWith("html")) {
        compressor.compress(res)
      } else {
        res
      }

      Right(result)
    } catch {
      case t: Throwable =>
        engine.handleException(t)
        Left(t)
    }

  }

}
