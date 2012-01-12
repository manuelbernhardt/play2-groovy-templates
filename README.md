# Groovy templates for Play! 2

Groovy template mechanism for Play! 2 (under development). The scala version looks like this:

    object Application extends Controller with GroovyTemplates {

      def controllerMethod = Action { implicit request =>
        Ok(Template('foo -> "bar"))
      }

    }

In order to try out this plugin, you need to:

- clone the groovy-templates-engine from https://github.com/manuelbernhardt/play-groovy-templates and publish it locally via `sbt publish-local`

- build this plugin & publish it locally, also via `sbt publish-local`

- you may have to copy the `JamonAPI` library by hand into your play2 ivy repository (until it is fixed)

Then, a build configuration for a play2 project to use the plugin look like this (e.g. for the computer-database example):

    object ApplicationBuild extends Build {

        val appName         = "computer-database"
        val appVersion      = "1.0"

        val appDependencies = Seq(
          "play" %% "groovy-templates" % "0.1-SNAPSHOT"
        )

        val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA)
          .settings(
            resolvers += Resolver.file("local-ivy-repo", file(Path.userHome+"/.ivy2/local"))(Resolver.ivyStylePatterns)
          )

    }