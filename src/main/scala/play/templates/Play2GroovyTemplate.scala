package play.templates

import play.exceptions._
import java.lang.Throwable
import TemplateEngineException.ExceptionType._
/**
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

class Play2GroovyTemplate(name: String, source: String) extends GroovyTemplate(name, source) {

  def this(source: String) {
    this("", source)
  }

  def handleException(e: TemplateEngineException) {
    e.getExceptionType match {
      case NO_ROUTE_FOUND =>
        // shouldn't happen
        throwException(e)
      case PLAY =>
        throwException(cleanStackTrace(e))

    }
  }

  def throwException(e: Throwable) {
    throw e
  }
}