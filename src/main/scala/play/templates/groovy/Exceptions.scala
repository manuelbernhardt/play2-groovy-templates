package play.templates.groovy

import play.api.PlayException.ExceptionSource
import play.api.PlayException
import java.io.File
import scalax.io.Input

/**
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

class TemplateExecutionException(description: String, l: Option[Int], p: Option[Int], file: Option[File], n: Option[String], i: Option[Input] = None)
  extends PlayException("Template Execution Exception", description: String) with ExceptionSource {
  def line = l
  def position = p
  def input = if(i.isDefined) i else file.map(scalax.file.Path(_))
  def sourceName = n
}

class TemplateCompilationException(description: String, l: Option[Int], p: Option[Int], file: Option[File], n: Option[String], i: Option[Input] = None)
  extends PlayException("Template Compilation Exception", description: String) with ExceptionSource {
  def line = l
  def position = p
  def input = if(i.isDefined) i else file.map(scalax.file.Path(_))
  def sourceName = n
}

class TemplateNotFoundException(description: String) extends PlayException("Template not found", description: String)