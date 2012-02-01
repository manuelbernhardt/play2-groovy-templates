package play.templates

import exceptions.TemplateExecutionException
import java.lang.Throwable
import TemplateEngineException.ExceptionType._
import play.exceptions.TagInternalException
import play.api.Play
import play.api.Play.current
import java.io.File
import scalax.file.Path

class Play2GroovyTemplate(name: String, source: String) extends GroovyTemplate(name, source) {

  def this(source: String) {
    this("", source)
  }

  def handleException(e: TemplateEngineException) {
    // TODO better handling
    e.getExceptionType match {
      case NO_ROUTE_FOUND =>
        // shouldn't happen
        throwException(e)
      case PLAY =>
        throwException(cleanStackTrace(e))
      case _ =>
        throwException(e)
    }
  }

  def throwException(e: Throwable) {

    try {
        for (stackTraceElement <- e.getStackTrace) {
          println(stackTraceElement.getClassName)
            if (stackTraceElement.getClassName.equals(compiledTemplateName) || stackTraceElement.getClassName.startsWith(compiledTemplateName + "$_run_closure")) {
                if (doBodyLines.contains(stackTraceElement.getLineNumber)) {
                    throw new TemplateExecutionException.DoBodyException(e)
                } else if(e.isInstanceOf[TemplateCompilationError]) {
                  throw cleanStackTrace(e).asInstanceOf[TemplateCompilationError]
                } else if (e.isInstanceOf[TagInternalException]) {
                    throw cleanStackTrace(e).asInstanceOf[TagInternalException]
                } else if (e.isInstanceOf[TemplateExecutionException]) {
                    val ex = e.asInstanceOf[TemplateExecutionException]
                    val pe = new groovy.TemplateExecutionException(
                      ex.getMessage,
                      Some(ex.getLineNumber.toInt),
                      None,
                      Some(new File(play.api.Play.current.path, ex.getTemplate.getName)),
                      Some(ex.getTemplate.getName)
                    )
                    throw cleanStackTrace(pe)
                } else {
                    val t = new groovy.TemplateExecutionException(
                      e.getMessage,
                      Some(this.linesMatrix.get(stackTraceElement.getLineNumber)),
                      None,
                      Some(new File(play.api.Play.current.path, this.getName)),
                      Some(this.getName)
                    )
                    throw cleanStackTrace(t)
                }
            }

          // heuristic to see if we can show something relevant, at all
          if(stackTraceElement.getLineNumber > 0 && stackTraceElement.getClassName.startsWith("/app/views")) {
            throw new groovy.TemplateExecutionException(
              e.getMessage,
              Some(stackTraceElement.getLineNumber),
              None,
              None,
              Some(this.getName),
              Some(Path(Play.getFile(stackTraceElement.getClassName)))
            )
          }
        }
        throw new RuntimeException(e);
    } catch {
      case t => TemplateEngine.engine.handleException(t);
    }
  }
}

