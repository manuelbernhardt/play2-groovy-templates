package play.templates

import java.lang.Throwable
import TemplateEngineException.ExceptionType._
import play.api.PlayException

/**
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

class Play2GroovyTemplate(name: String, source: String) extends GroovyTemplate(name, source) {

  def this(source: String) {
    this("", source)
  }

  def handleException(e: TemplateEngineException) {
    // TODO better handling
    e.printStackTrace()
    e.getExceptionType match {
      case NO_ROUTE_FOUND =>
        // shouldn't happen
        throwException(e)
      case PLAY =>
        throwException(cleanStackTrace(e))
      case _ => throwException(e)
    }
  }

  def throwException(e: Throwable) {
    throw PlayException("Error", e.getMessage, Some(e))
  }
}