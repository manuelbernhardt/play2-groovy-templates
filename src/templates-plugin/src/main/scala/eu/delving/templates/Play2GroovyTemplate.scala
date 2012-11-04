package eu.delving.templates

import _root_.java.io.File
import _root_.java.net.URL
import play.exceptions.TagInternalException
import play.api.Play
import play.api.Play.current
import scalax.file.Path
import play.templates.TemplateEngineException.ExceptionType._
import play.templates.{TemplateEngine, TemplateCompilationError, TemplateEngineException, GroovyTemplate}
import play.templates.exceptions.TemplateExecutionException

class Play2GroovyTemplate(name: String, source: String) extends GroovyTemplate(name, source) {

  def this(source: String) {
    this("", source)
  }

  override def handleException(e: TemplateEngineException) {
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

  override def throwException(e: Throwable) {

    try {
        for (stackTraceElement <- e.getStackTrace) {
          val cName: String = stackTraceElement.getClassName

//          println(cName + " " + stackTraceElement.getLineNumber)
            if (cName.equals(compiledTemplateName) || cName.startsWith(compiledTemplateName + "$_run_closure")) {
                if (doBodyLines.contains(stackTraceElement.getLineNumber)) {
                    throw new TemplateExecutionException.DoBodyException(e)
                } else if(e.isInstanceOf[TemplateCompilationError]) {
                  throw cleanStackTrace(e).asInstanceOf[TemplateCompilationError]
                } else if (e.isInstanceOf[TagInternalException]) {
                    throw cleanStackTrace(e).asInstanceOf[TagInternalException]
                } else if (e.isInstanceOf[TemplateExecutionException]) {
                    val ex = e.asInstanceOf[TemplateExecutionException]
                    val pe = new eu.delving.templates.exceptions.TemplateExecutionException(
                      ex.getMessage,
                      Some(ex.getLineNumber.toInt),
                      None,
                      Some(new File(play.api.Play.current.path, ex.getTemplate.getName)),
                      Some(ex.getTemplate.getName)
                    )
                    throw cleanStackTrace(pe)
                } else if(e.isInstanceOf[eu.delving.templates.exceptions.TemplateExecutionException]) {
                  throw e
                } else {
                    val t = new eu.delving.templates.exceptions.TemplateExecutionException(
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
          val candidate = stackTraceElement.getLineNumber > 0 && (
            cName.startsWith("/app/views") ||
            cName.startsWith("controllers.") ||
            cName.startsWith("views.")
            )

          if(candidate) {

            val className = if(cName.endsWith("$")) cName.substring(0, cName.length() - 1) else cName
            val fileName = if(!className.startsWith("/app/views")) {
              // TODO will not work for modules
              val path = "/app/" + className.replaceAll("\\.", "/")
              val scala = path  + ".scala"
              val java = path + ".java"
              if(Play.getExistingFile(scala).isDefined) scala else java
            } else className

            throw new eu.delving.templates.exceptions.TemplateExecutionException(
              e.getMessage,
              Some(stackTraceElement.getLineNumber),
              None,
              None,
              Some(fileName),
              Play.resource(fileName).flatMap(r => Path(r.toURI))
            )
          }
        }
        throw new RuntimeException(e)
    } catch {
      case t: Throwable => TemplateEngine.engine.handleException(t)
    }
  }
}

