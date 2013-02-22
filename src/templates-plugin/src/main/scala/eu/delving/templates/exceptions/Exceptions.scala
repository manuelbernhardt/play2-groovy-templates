package eu.delving.templates.exceptions

import play.api.PlayException.ExceptionSource
import play.api.PlayException
import java.io.File
import scalax.io.Input

/**
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

class TemplateExecutionException(description: String, l: Option[Int], p: Option[Int], file: Option[File], n: Option[String], i: Option[Input] = None)
  extends PlayException.ExceptionSource("Template Execution Exception", description: String) {
  def line: java.lang.Integer = l.map(_.asInstanceOf[java.lang.Integer]).orNull

  def position = p.map(_.asInstanceOf[java.lang.Integer]).orNull

  def input = if (i.isDefined) i.get.toString else file.map(scalax.file.Path(_)).get.toString

  def sourceName = n.orNull
}

class TemplateCompilationException(description: String, l: Option[Int], p: Option[Int], file: Option[File], n: Option[String], i: Option[Input] = None)
  extends PlayException.ExceptionSource("Template Compilation Exception", description: String) {
  def line = l.map(_.asInstanceOf[java.lang.Integer]).orNull

  def position = p.map(_.asInstanceOf[java.lang.Integer]).orNull

  def input = if (i.isDefined) i.get.toString else file.map(scalax.file.Path(_)).get.toString

  def sourceName = n.orNull
}

class TemplateNotFoundException(description: String) extends PlayException("Template not found", description: String)