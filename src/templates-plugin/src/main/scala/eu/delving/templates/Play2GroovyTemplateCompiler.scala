package eu.delving.templates

import _root_.java.io.File
import exceptions.TemplateCompilationException
import play.templates.GroovyTemplateCompiler
import play.api.Play.current

class Play2GroovyTemplateCompiler extends GroovyTemplateCompiler {

  override def action(absolute: Boolean) {

    def invalidRouteDefinition(action: String) {
      throw new TemplateCompilationException(
        "Invalid routes definition: %s".format(action),
        Some(parser.getLine),
        Some(template.source.split("\n")(parser.getLine).indexOf(parser.getToken)),
        Some(new File(play.api.Play.current.path, template.getName)),
        Some(template.name),
        None
      )
    }

    val actionPattern = """^(.*)?routes(.*)$""".r
    val assetsPatternSingleQuote = "'(.*?)'".r
    val assetsPatternDoubleQuote = """"(.*?)"""".r
    val action: String = parser.getToken.trim

    // Play 2 routes live in a routes package object
    val matched = actionPattern.findFirstMatchIn(action)
    if (matched.isDefined) {
      val group: String = matched.get.group(2)

      if (group.startsWith(".Assets.at(")) {
        val matchedAsset = Option(assetsPatternSingleQuote.findFirstMatchIn(group).getOrElse(assetsPatternDoubleQuote.findFirstMatchIn(group).getOrElse(null)))
        if (matchedAsset.isDefined) {
          if(absolute) {
            super.println("\tout.print('http://' + _('httpRequest').host + _('controllers.routes').Assets.at(\"" + matchedAsset.get.group(1) + "\").url());")
          } else {
            super.println("\tout.print(_('controllers.routes').Assets.at(\"" + matchedAsset.get.group(1) + "\").url());")
          }
        } else {
          invalidRouteDefinition(action)
        }
      } else {
        if(absolute) {
          super.print("\tout.print('http://' + _('httpRequest').host + _('" + matched.get.group(1) + "routes')" + group + ");")
        } else {
          super.print("\tout.print(_('" + matched.get.group(1) + "routes')" + group + ");")
        }
      }
    } else {
      invalidRouteDefinition(action)
    }
    markLine(parser.getLine)
    super.println()
  }
}