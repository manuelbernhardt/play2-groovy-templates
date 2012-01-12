# Groovy templates for Play! 2

Groovy template mechanism for Play! 2 (under development). The scala version looks like this:

    object Application extends Controller with GroovyTemplates {

      def controllerMethod = Action { implicit request =>
        Ok(Template('foo -> "bar"))
      }

    }