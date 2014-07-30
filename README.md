# Groovy templates for Play! 2

Groovy template mechanism for Play! 2, to make the migration between Play 1 and Play 2 easier. The template engine is based on the one of Play 1.

More information and documentation about the template engine can be found here:

- [Groovy Templates engine documentation](http://www.playframework.org/documentation/1.2.5/templates)
- [Groovy Tags documentation](http://www.playframework.org/documentation/1.2.5/tags)

In order to use the plugin, make sure you have these dependencies / resolvers in your SBT build:

- dependencies: `"io.bernhardt" %% "groovy-templates-plugin" % "1.6.4-SNAPSHOT"`
- resolvers:
  - `"Sonatype OSS Repository" at "https://oss.sonatype.org/content/groups/public"`

In order for pre-compilation to work correctly in PROD mode, you need to hook the groovy templates plugin in the `sourceGenerators` of your build, for example:

    import eu.delving.templates.Plugin._

    val main = PlayProject(appName, appVersion, appDependencies, settings = Defaults.defaultSettings ++ groovyTemplatesSettings).settings(

      sourceGenerators in Compile <+= groovyTemplatesList,
    
    )

And your `project/plugins.sbt` needs to contain the Groovy Templates SBT plugin:

    resolvers ++= Seq(
       "Sonatype OSS Repository" at "https://oss.sonatype.org/content/groups/public"
    )
    
    addSbtPlugin("eu.delving" %% "groovy-templates-sbt-plugin" % "1.6.4-SNAPSHOT")


(note: this will scan for templates at compilation time and generate a list which is included in the build and used in PROD mode to pre-compile the templates. We need this because Groovy Templates aren't compiled source-files)

### Disabling the reloading of the application when a template file is modified

You can do this by excluding the HTML template files from the `watchTransitiveSources` task of your project, e.g. by adding this definition into your main Project definition:

    watchTransitiveSources <<= watchTransitiveSources map { (sources: Seq[java.io.File]) =>
      sources
        .filterNot(source => source.isFile && source.getPath.contains("app/views") && !source.getName.endsWith(".scala.html") && source.getName.endsWith(".html"))
        .filterNot(source => source.isDirectory && source.getPath.contains("app/views"))
    }

(this is planned to be done automatically by the SBT plugin in a future release)

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

- autheniticty token: Play 1 has the concept of authenticity token which is used e.g. in the `#{authenticityToken /}` tag. If you want to set one for template rendering, you can do this by passing it to the arguments as `__AUTH_TOKEN` parameter


## Java

The Java API is less developed than the Scala one, but it is there nonetheless. In order to use it you need to extend the `eu.delving.templates.java.GroovyTemplatesController`. Then you can call templates like this:

    public static Result index() {
      return ok(
        Template("index.html").params("foo", "bar", "foo2", 42).render()
      );
    }

This will render the template `/app/views/index.html` and pass the parameters `foo` with the value `bar` and `foo2` with the value `42`.

- i18n: By default, the language resolved via the `lang()` call of the controller is used. This can be overriden by passing in a `__LANG` parameter

## Settings

The following settings (in `application.conf` or whever you application's configuration lives) influence the plugin's behaviour:

- `play.groovyTemplates.enabled`: whether or not the plugin is enabled (default: `true`)
- `play.groovyTemplates.htmlCompression`: activates HTML compression of rendered templates (default: `true`)

## Changelog

### 1.6.2 - 26.08.2013

- Implicit name resolution works as well with injected controllers
- Faster reflections discovery

### 1.6.1 - 21.02.2013

- tinoadams: fix for issue #8 - dropping scala version for sbt pluging to 2.9.2 

### 1.6.0 - 20.02.2013

- Play 2.1.0
- Scala 2.10.0
- Breaking change: fixing a conversion issue with the `params` variable in the view which was causing them to be passed in as Scala Buffer instead of Java ArrayList
- tinoadams: fix for issue #6 - java.util.regex.PatternSyntaxException on Windows

### 1.5.4 - 2.11.2012

- only attempting to compute current execution method if no template name is passed
- Play 2.0.4

### 1.5.3 - 24.10.2012

- making it possible to disable the plugin by setting `play.groovyTemplates.enabled=false` in the application's configuration file

### 1.5.2 - 28.08.2012

- using SBT project build dependency graph in order to discover templates instead of the hardcoded lookup in "modules"

### 1.5.1 - 08.08.2012

- fixing memory leak: for controllers mixing in the GroovyTemplates trait but never calling Template, render arguments were never flushed 
- update to Play 2.0.3

### 1.5 - 27.07.2012

- Breaking API change: using mutable HashMap instead of custom RenderArgs
- Fixing memory leak: the renderArgs per-request cache now uses Google Guava's CacheBuilder, which by default has weakly referenced keys
- Escaping paths generated by GroovyTemplatesList for Windows

### 1.4 - 4.07.2012

- Upgrade to Play 2.0.2
- Upgrade to SBT 0.11.3

### 1.3 - 25.06.2012

- Depending on Play 2.0.1
- support for Play 2.0 stage / dist mode: no longer looking up files on disk in PROD, instead relying on the classpath. This is a breaking change in that it is necessary to add a SBT plugin to the build in order for pre-compilation to work.
- preserving HTML comments in PROD mode: this is necessary for e.g. libraries such as KnockoutJS (will be made configurable)

### 1.2 - 4.06.2012

- compressing HTML in PROD mode
- support for absolute routes

### 1.1 - 21.03.2012

- fixing issue with access to language, messages and render args
- using play-groovy-templates 0.6: passing all necessary values for template rendering at template invocation time 
- improvements in the Java API

### 1.0 - 13.03.2012
