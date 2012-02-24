package eu.delving.templates

import _root_.java.io.File
import play.api.Play.current
import play.templates.{GroovyTemplateCompiler, TemplateCompilationError}

class Play2GroovyTemplateCompiler extends GroovyTemplateCompiler {

  override def action(absolute: Boolean) {

    def invalidRouteDefinition(action: String) {
      throw new TemplateCompilationError(new File(current.path, template.name), "Invalid routes definition: %s".format(action), parser.getLine, template.source.split("\n")(parser.getLine).indexOf(parser.getToken))
    }

    val actionPattern = """^(.*)?routes(.*)$""".r
    val assetsPatternSingleQuote = "'(.*?)'".r
    val assetsPatternDoubleQuote = """"(.*?)"""".r
    val action: String = parser.getToken.trim

    // TODO absolute actions @@{ ... }
    // Play 2 routes live in a routes package object
    val matched = actionPattern.findFirstMatchIn(action)
    if (matched.isDefined) {
      val group: String = matched.get.group(2)

      if (group.startsWith(".Assets.at(")) {
        val matchedAsset = Option(assetsPatternSingleQuote.findFirstMatchIn(group).getOrElse(assetsPatternDoubleQuote.findFirstMatchIn(group).getOrElse(null)))
        if (matchedAsset.isDefined) {
          super.println("\tout.print(_('controllers.routes').Assets.at(\"" + matchedAsset.get.group(1) + "\").url());")
        } else {
          invalidRouteDefinition(action)
        }
      } else {
        super.print("\tout.print(_('" + matched.get.group(1) + "routes')" + group + ");")
      }
    } else {
      invalidRouteDefinition(action)
    }
    markLine(parser.getLine)
    super.println()
  }
}