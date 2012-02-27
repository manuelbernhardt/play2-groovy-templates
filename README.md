# Groovy templates for Play! 2

Groovy template mechanism for Play! 2, to make the migration between Play 1 and Play 2 easier. The template engine is based on the one of Play 1, and this plugin makes the integration with Play 2 possible. Due to some differences in the API not all things are working yet in Play 2, but most of them are.

In order to use the plugin, make sure you have these dependencies / resolvers in your SBT build:

- dependencies: `"eu.delving" %% "groovy-templates-plugin" % "1.0-SNAPSHOT"`
- resolvers:
  - `"Delving Releases Repository" at "http://development.delving.org:8081/nexus/content/groups/public"`
  - `"Delving Snapshot Repository" at "http://development.delving.org:8081/nexus/content/repositories/snapshots"`


## Scala

In order to use the Groovy templates with Scala, mix in the `eu.delving.templates.scala.GroovyTemplates` trait. Then you can call templates like this:


     object Application extends Controller with GroovyTemplates {
     
       def fooAction = Action { implicit request =>
         Ok(Template('foo -> "bar"))
       }

    }

This will render the `/app/views/fooAction.html` template, and pass the argument `foo` with the value `bar`.

- the `Template` method takes a sequence of `(Symbol, AnyRef)` arguments that are passed to the template, so you can call things like `Template('foo -> "bar", 'foo2 -> bar2)`

- by default, the template name is resolved by convention using the name of the action method. If you want to render another template, use `Template("myOtherTemplate.html", 'foo -> "bar")`

- There is a mutable `renderArgs` map in scope to which you can add things like e.g. this`renderArgs += ("foo", "bar")`. It is useful in e.g. action composition.

- i18n: By default, the language resolved via the implicit `lang` method is used. If however for whatever reason you need to override this at template render time (which may happen if you implement your own session-based language management), you can override the language by passing a `__LANG` parameter to the arguments (or renderArgs). In this case, you need to pass a language code.

- sessionId: Play 1 has the concept of sessionId which is used e.g. in the `#{authenticityToken /}` tag. If you want to set one for template rendering, you can do this by passing it to the arguments as `__SESSION_ID` parameter


## Java

The Java API is less developed than the Scala one, but it is there nonetheless. In order to use it you need to extend the `eu.delving.templates.java.GroovyTemplatesController`. Then you can call templates like this:

    public static Result index() {
      return ok(
        Template("index.html").params("foo", "bar", "foo2", 42).render()
      );
    }

This will render the template `/app/views/index.html` and pass the parameters `foo` with the value `bar` and `foo2` with the value `42`.


