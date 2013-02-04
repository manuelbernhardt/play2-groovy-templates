package eu.delving.templates

/**
 * Groovy tweaks, taken from the play-scala plugin
 */
object CustomGroovy {

    def apply() {

        import _root_.groovy.lang._

        new GroovyShell().evaluate("""

            ExpandoMetaClass.enableGlobally()

            java.lang.Object.metaClass.propertyMissing = { name ->
                try {
                    delegate.getClass().getMethod(name)
                } catch(NoSuchMethodException e) {
                    throw new MissingPropertyException(name, delegate.getClass())
                }
                throw new MissingPropertyException("No such property: ${name} for class: ${delegate.getClass()}. Try a method call ${name}() instead", name, delegate.getClass())
            }

            java.lang.Object.metaClass.safeNull = { -> delegate }

            scala.Option.metaClass.safeNull = { ->
                if(delegate.isDefined()) {
                    delegate.get()
                } else {
                    null
                }
            }

            scala.Option.metaClass.toString = { ->
                if(delegate.isDefined()) {
                    delegate.get().toString()
                } else {
                    null
                }
            }

// we have to comment these out for the time being
// because there appears to be something strange with regards to how these Scala classes
// answer to getInterfaces() and getGenericInterfaces()

//            scala.Some.metaClass.toString = { ->
//                delegate.get().toString()
//            }
//
//            scala.None.metaClass.toString = { ->
//                null
//            }

//            scala.Option.metaClass.asBoolean = { ->
//                delegate.isDefined()
//            }
//
//            scala.collection.Seq.metaClass.asBoolean = { ->
//                delegate.size() > 0
//            }
//
//            scala.collection.Seq.metaClass.getAt = { i ->
//                delegate.apply(i)
//            }

        """)
    }

}
