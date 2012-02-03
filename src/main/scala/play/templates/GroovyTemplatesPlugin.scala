package play.templates

import play.api._
import org.reflections._
import scala.collection.JavaConverters._
import collection.mutable.HashMap
import java.util.ArrayList
import java.util.concurrent.ConcurrentHashMap

/**
 * Plugin for rendering Groovy templates
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

class GroovyTemplatesPlugin(app: Application) extends Plugin {

  override def enabled = true

  var engine: TemplateEngine = null

  var allClassesMetadata: Reflections = null

  var assignableClassesCache = new HashMap[Class[_], ArrayList[Class[_]]]

  var allClassesCache = null


  override def onStart {
    engine = new Play2TemplateEngine
    engine.startup()
    
    // cache lookup of classes
    // the template engine needs this to allow static access to classes with "nice" names (without the $'s)
    allClassesMetadata = new Reflections(new util.ConfigurationBuilder()
      .addUrls(util.ClasspathHelper.forJavaClassPath())
      .setScanners(new AllTypesScanner, new scanners.SubTypesScanner))

    Logger("play").debug("Found %s classes".format(allClassesMetadata.getStore.getKeysCount))

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

  def getAllClasses = {
    val classes = allClassesMetadata.getStore.get(classOf[AllTypesScanner]).keySet()
    val list = new ArrayList[Class[_]]()
    for (c <- classes.asScala) {
      try {
        list.add(app.classloader.loadClass(c))
      } catch {
        case t => // we don't care
      }

    }
    list

  }

  def renderTemplate(name: String, args: Map[String, AnyRef]) = {

    try {
      val n = System.currentTimeMillis()
      Logger("play").debug("Loading template " + name)
      val template = GenericTemplateLoader.load(name)
      Logger("play").debug("Starting to render")
      val templateArgs = new ConcurrentHashMap[String, AnyRef](args.asJava)
      val res = template.render(templateArgs)
      Logger("play").info("Rendered template %s in %s".format(name, System.currentTimeMillis() - n))
      Right(res)
    } catch {
      case t: Throwable =>
        engine.handleException(t)
        Left(t)
    }

  }

}
