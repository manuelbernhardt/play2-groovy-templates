package play.templates

import exceptions.TemplateCompilationException

/**
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

class Play2GroovyTemplateCompiler extends GroovyTemplateCompiler {
  
  override def action(absolute: Boolean) {
    val actionPattern = """^(.*)routes(.*)$""".r
    
    val action: String = parser.getToken.trim

    // TODO absolute actions @@{ ... }
    // Play 2 routes live in a routes package object
    val matched = actionPattern.findFirstMatchIn(action)
    if (matched.isDefined) {
      print("\tout.print(_('" + matched.get.group(1) + "routes')" + matched.get.group(2) + ");")
    } else {
      throw new TemplateCompilationException(template, parser.getLine, "Route definition not found: '%s'".format(action))
    }
    markLine(parser.getLine)
    println()
  }
}