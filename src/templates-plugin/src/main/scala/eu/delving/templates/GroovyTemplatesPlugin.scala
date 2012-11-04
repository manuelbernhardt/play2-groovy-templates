package eu.delving.templates

import _root_.java.util.ArrayList
import _root_.java.util.concurrent.ConcurrentHashMap
import play.api._
import play.api.Play.current
import org.reflections._
import _root_.scala.collection.JavaConverters._
import collection.mutable.HashMap
import play.templates.{GenericTemplateLoader, TemplateEngine}
import com.googlecode.htmlcompressor.compressor.HtmlCompressor
import play.templates.exceptions.TemplateCompilationException

/**
 * Plugin for rendering Groovy templates
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */

class GroovyTemplatesPlugin(app: Application) extends Plugin {

  override def enabled = app.configuration.getBoolean("play.groovyTemplates.enabled").getOrElse(true)

  val compressor = new HtmlCompressor()
  compressor.setRemoveComments(false)

  var engine: TemplateEngine = null

  var allClassesMetadata: Reflections = null

  var assignableClassesCache = new HashMap[Class[_], ArrayList[Class[_]]]

  var allClassesCache = new ArrayList[Class[_]]

  override def onStart {
    engine = new Play2TemplateEngine
    engine.startup()

    // cache lookup of classes
    // the template engine needs this to allow static access to classes with "nice" names (without the $'s)
    // we also use this to find FastTag-s and JavaExtension-s, assuming they live in "views"
    allClassesMetadata = new Reflections(new util.ConfigurationBuilder()
      .addUrls(util.ClasspathHelper.forJavaClassPath())
      .addUrls(util.ClasspathHelper.forPackage("play.templates", TemplateEngine.utils.getClassLoader))
      .addUrls(util.ClasspathHelper.forPackage("views", TemplateEngine.utils.getClassLoader))
      .setScanners(new AllTypesScanner, new scanners.SubTypesScanner))

    Logger("play").debug("Found %s classes".format(allClassesMetadata.getStore.getKeysCount))

    getAllClasses

    if(TemplateEngine.utils.usePrecompiled()) {
      Logger("play").info("Precompiling...")

      try {
        engine.asInstanceOf[Play2TemplateEngine].templatesList.templates.map {
          template => {
            val loaded = GenericTemplateLoader.load(template)
            if (loaded != null) {
              try {
                  loaded.compile();
              } catch {
                case tce: TemplateCompilationException => {
                  TemplateEngine.utils.logError("Template %s does not compile at line %d", tce.getTemplate().name, tce.getLineNumber());
                  throw tce
                }
              }
            }
          }
        }
      } catch {
        case t: Throwable => TemplateEngine.engine.handleException(t)
      }
    }

    CustomGroovy()

    Logger("play").info("Groovy template engine started")
  }

  override def onStop {
    Logger("play").info("Stopping Groovy template engine")
  }

  def getAssignableClasses(clazz: Class[_]) = {
    if (assignableClassesCache.contains(clazz)) {
      assignableClassesCache(clazz)
    } else {
      val assignableClasses = allClassesMetadata.getSubTypesOf(clazz)
      val list = new ArrayList[Class[_]]()
      list.addAll(assignableClasses)
      assignableClassesCache.put(clazz, list)
      list
    }
  }

  // TODO re-think this
  // this used to fetch only application classes, and probably some more
  // however the reason for it seems a little obscure, so we'll just take in the bare minimum necessary for things to work nicely
  def getAllClasses = {
    if(allClassesCache.isEmpty) {
      val toLoad = Seq(
"scala.Array$",
"scala.Array$$anon$2",
"scala.Console$",
"scala.Equals",
"scala.FallbackArrayBuilding",
"scala.Function0",
"scala.Function1",
"scala.Function1$mcII$sp",
"scala.Function2",
"scala.Function3",
"scala.LowPriorityImplicits",
"scala.MatchError",
"scala.None$",
"scala.Option",
"scala.Option$",
"scala.Option$$anonfun$orNull$1",
"scala.Option$WithFilter",
"scala.Predef$",
"scala.Predef$$anon$3",
"scala.Predef$$anon$4",
"scala.Predef$$eq$colon$eq",
"scala.Predef$$less$colon$less",
"scala.Product",
"scala.Product$$anon$1",
"scala.Product2",
"scala.Product3",
"scala.Product4",
"scala.Proxy",
"scala.Serializable",
"scala.Some",
"scala.SpecializableCompanion",
"scala.Tuple2",
"scala.Tuple3",
"scala.Tuple4",
"scala.collection.GenIterableLike",
"scala.collection.GenMap",
"scala.collection.GenMapLike",
"scala.collection.GenMapLike$$anonfun$liftedTree1$1$1",
"scala.collection.GenSeq",
"scala.collection.GenSeqLike",
"scala.collection.GenSet",
"scala.collection.GenSetLike",
"scala.collection.GenTraversableLike",
"scala.collection.GenTraversableOnce",
"scala.collection.IndexedSeq",
"scala.collection.IndexedSeqLike",
"scala.collection.IndexedSeqLike$Elements",
"scala.collection.IndexedSeqOptimized",
"scala.collection.IndexedSeqOptimized$$anon$1",
"scala.collection.IndexedSeqOptimized$$anonfun$exists$1",
"scala.collection.IndexedSeqOptimized$$anonfun$forall$1",
"scala.collection.Iterable",
"scala.collection.Iterable$",
"scala.collection.IterableLike",
"scala.collection.Iterator",
"scala.collection.Iterator$",
"scala.collection.Iterator$$anon$12",
"scala.collection.Iterator$$anon$18",
"scala.collection.Iterator$$anon$19",
"scala.collection.Iterator$$anon$21",
"scala.collection.Iterator$$anon$3",
"scala.collection.Iterator$$anonfun$toStream$1",
"scala.collection.JavaConversions$",
"scala.collection.JavaConversions$JMapWrapper",
"scala.collection.JavaConversions$JMapWrapperLike$$anon$2",
"scala.collection.JavaConversions$MapWrapper",
"scala.collection.JavaConversions$MapWrapper$$anon$1",
"scala.collection.JavaConversions$MapWrapper$$anon$1$$anon$5",
"scala.collection.JavaConversions$MapWrapper$$anon$1$$anon$5$$anon$6",
"scala.collection.JavaConversions$MutableMapWrapper",
"scala.collection.LinearSeq",
"scala.collection.LinearSeqLike",
"scala.collection.LinearSeqLike$$anon$1",
"scala.collection.LinearSeqOptimized",
"scala.collection.Map$",
"scala.collection.MapLike",
"scala.collection.MapLike$$anon$3",
"scala.collection.MapLike$$anonfun$addString$1",
"scala.collection.MapLike$$anonfun$filterNot$1",
"scala.collection.MapLike$DefaultKeySet",
"scala.collection.MapLike$DefaultKeySet$$anonfun$foreach$1",
"scala.collection.MapLike$DefaultKeySet$$anonfun$foreach$2",
"scala.collection.Parallelizable",
"scala.collection.Seq",
"scala.collection.Seq$",
"scala.collection.SeqLike",
"scala.collection.SeqLike$$anonfun$contains$1",
"scala.collection.SeqLike$$anonfun$distinct$1",
"scala.collection.SeqLike$$anonfun$reverse$1",
"scala.collection.SeqLike$$anonfun$reverse$2",
"scala.collection.Set$",
"scala.collection.SetLike",
"scala.collection.SetLike$$anonfun$$plus$plus$1",
"scala.collection.TraversableLike",
"scala.collection.TraversableLike$$anonfun$filter$1",
"scala.collection.TraversableLike$$anonfun$filterNot$1",
"scala.collection.TraversableLike$$anonfun$flatMap$1",
"scala.collection.TraversableLike$$anonfun$last$1",
"scala.collection.TraversableLike$$anonfun$map$1",
"scala.collection.TraversableLike$WithFilter$$anonfun$foreach$1",
"scala.collection.TraversableOnce",
"scala.collection.TraversableOnce$$anonfun$addString$1",
"scala.collection.TraversableOnce$$anonfun$foldLeft$1",
"scala.collection.TraversableOnce$$anonfun$size$1",
"scala.collection.generic.CanBuildFrom",
"scala.collection.generic.FilterMonadic",
"scala.collection.generic.GenMapFactory",
"scala.collection.generic.GenMapFactory$MapCanBuildFrom",
"scala.collection.generic.GenSeqFactory",
"scala.collection.generic.GenSetFactory",
"scala.collection.generic.GenTraversableFactory",
"scala.collection.generic.GenericCompanion",
"scala.collection.generic.GenericSetTemplate",
"scala.collection.generic.GenericTraversableTemplate",
"scala.collection.generic.Growable",
"scala.collection.generic.Growable$$anonfun$$plus$plus$eq$1",
"scala.collection.generic.ImmutableMapFactory",
"scala.collection.generic.ImmutableSetFactory",
"scala.collection.generic.IterableForwarder",
"scala.collection.generic.MapFactory",
"scala.collection.generic.MutableSetFactory",
"scala.collection.generic.SeqFactory",
"scala.collection.generic.SeqForwarder",
"scala.collection.generic.SetFactory",
"scala.collection.generic.TraversableForwarder",
"scala.collection.immutable.$colon$colon",
"scala.collection.immutable.HashMap",
"scala.collection.immutable.HashMap$",
"scala.collection.immutable.HashMap$EmptyHashMap$",
"scala.collection.immutable.HashMap$HashMap1",
"scala.collection.immutable.HashMap$HashMapCollision1",
"scala.collection.immutable.HashMap$HashMapCollision1$$anonfun$updated0$1",
"scala.collection.immutable.HashMap$HashMapCollision1$$anonfun$updated0$2",
"scala.collection.immutable.HashMap$HashTrieMap",
"scala.collection.immutable.HashMap$HashTrieMap$$anon$1",
"scala.collection.immutable.HashSet",
"scala.collection.immutable.HashSet$",
"scala.collection.immutable.HashSet$EmptyHashSet$",
"scala.collection.immutable.HashSet$HashSet1",
"scala.collection.immutable.HashSet$HashSetCollision1",
"scala.collection.immutable.HashSet$HashSetCollision1$$anonfun$updated0$1",
"scala.collection.immutable.HashSet$HashTrieSet",
"scala.collection.immutable.HashSet$HashTrieSet$$anon$1",
"scala.collection.immutable.IndexedSeq$",
"scala.collection.immutable.Iterable$",
"scala.collection.immutable.List",
"scala.collection.immutable.List$",
"scala.collection.immutable.List$$anonfun$toStream$1",
"scala.collection.immutable.ListMap",
"scala.collection.immutable.ListMap$",
"scala.collection.immutable.ListMap$$anon$1",
"scala.collection.immutable.ListMap$EmptyListMap$",
"scala.collection.immutable.ListMap$Node",
"scala.collection.immutable.ListSet",
"scala.collection.immutable.ListSet$",
"scala.collection.immutable.ListSet$$anon$1",
"scala.collection.immutable.ListSet$EmptyListSet$",
"scala.collection.immutable.ListSet$ListSetBuilder",
"scala.collection.immutable.ListSet$ListSetBuilder$$anonfun$result$1",
"scala.collection.immutable.ListSet$Node",
"scala.collection.immutable.Map",
"scala.collection.immutable.Map$",
"scala.collection.immutable.Map$EmptyMap$",
"scala.collection.immutable.Map$Map1",
"scala.collection.immutable.Map$Map2",
"scala.collection.immutable.Map$Map3",
"scala.collection.immutable.Map$Map4",
"scala.collection.immutable.MapLike",
"scala.collection.immutable.MapLike$ImmutableDefaultKeySet",
"scala.collection.immutable.Nil$",
"scala.collection.immutable.Seq",
"scala.collection.immutable.Seq$",
"scala.collection.immutable.Set",
"scala.collection.immutable.Set$",
"scala.collection.immutable.Set$EmptySet$",
"scala.collection.immutable.Set$Set1",
"scala.collection.immutable.Set$Set2",
"scala.collection.immutable.Set$Set3",
"scala.collection.immutable.Set$Set4",
"scala.collection.immutable.Stream",
"scala.collection.immutable.Stream$",
"scala.collection.immutable.Stream$$anonfun$$plus$plus$1",
"scala.collection.immutable.Stream$$anonfun$1",
"scala.collection.immutable.Stream$$anonfun$append$1",
"scala.collection.immutable.Stream$$anonfun$distinct$1",
"scala.collection.immutable.Stream$$anonfun$distinct$1$$anonfun$apply$1",
"scala.collection.immutable.Stream$$anonfun$filteredTail$1",
"scala.collection.immutable.Stream$$anonfun$flatMap$1",
"scala.collection.immutable.Stream$$anonfun$map$1",
"scala.collection.immutable.Stream$$anonfun$take$1",
"scala.collection.immutable.Stream$$anonfun$take$2",
"scala.collection.immutable.Stream$Cons",
"scala.collection.immutable.Stream$ConsWrapper",
"scala.collection.immutable.Stream$Empty$",
"scala.collection.immutable.Stream$StreamBuilder",
"scala.collection.immutable.Stream$StreamBuilder$$anonfun$result$1",
"scala.collection.immutable.Stream$StreamCanBuildFrom",
"scala.collection.immutable.Stream$StreamWithFilter",
"scala.collection.immutable.Stream$cons$",
"scala.collection.immutable.StreamIterator",
"scala.collection.immutable.StreamIterator$$anonfun$2",
"scala.collection.immutable.StreamIterator$$anonfun$next$1",
"scala.collection.immutable.StreamIterator$$anonfun$toStream$1",
"scala.collection.immutable.StreamIterator$LazyCell",
"scala.collection.immutable.StringLike",
"scala.collection.immutable.StringOps",
"scala.collection.immutable.TrieIterator",
"scala.collection.immutable.Vector",
"scala.collection.immutable.Vector$",
"scala.collection.immutable.Vector$$anon$1",
"scala.collection.immutable.Vector$$anon$2",
"scala.collection.immutable.VectorBuilder",
"scala.collection.immutable.VectorIterator",
"scala.collection.immutable.VectorPointer",
"scala.collection.immutable.WrappedString",
"scala.collection.immutable.WrappedString$",
"scala.collection.immutable.WrappedString$$anonfun$newBuilder$1",
"scala.collection.mutable.ArrayBuffer",
"scala.collection.mutable.ArrayBuffer$",
"scala.collection.mutable.ArrayBuilder",
"scala.collection.mutable.ArrayBuilder$",
"scala.collection.mutable.ArrayBuilder$ofBoolean",
"scala.collection.mutable.ArrayBuilder$ofByte",
"scala.collection.mutable.ArrayBuilder$ofChar",
"scala.collection.mutable.ArrayBuilder$ofDouble",
"scala.collection.mutable.ArrayBuilder$ofFloat",
"scala.collection.mutable.ArrayBuilder$ofInt",
"scala.collection.mutable.ArrayBuilder$ofLong",
"scala.collection.mutable.ArrayBuilder$ofRef",
"scala.collection.mutable.ArrayBuilder$ofShort",
"scala.collection.mutable.ArrayBuilder$ofUnit",
"scala.collection.mutable.ArrayOps",
"scala.collection.mutable.ArrayOps$ofBoolean",
"scala.collection.mutable.ArrayOps$ofByte",
"scala.collection.mutable.ArrayOps$ofChar",
"scala.collection.mutable.ArrayOps$ofDouble",
"scala.collection.mutable.ArrayOps$ofFloat",
"scala.collection.mutable.ArrayOps$ofInt",
"scala.collection.mutable.ArrayOps$ofLong",
"scala.collection.mutable.ArrayOps$ofRef",
"scala.collection.mutable.ArrayOps$ofShort",
"scala.collection.mutable.ArrayOps$ofUnit",
"scala.collection.mutable.Builder",
"scala.collection.mutable.Builder$$anon$1",
"scala.collection.mutable.Cloneable",
"scala.collection.mutable.FlatHashTable$",
"scala.collection.mutable.FlatHashTable$$anon$1",
"scala.collection.mutable.FlatHashTable$HashUtils",
"scala.collection.mutable.FlatHashTable$class",
"scala.collection.mutable.GrowingBuilder",
"scala.collection.mutable.HashSet",
"scala.collection.mutable.HashSet$",
"scala.collection.mutable.HashTable$",
"scala.collection.mutable.IndexedSeq",
"scala.collection.mutable.IndexedSeq$",
"scala.collection.mutable.IndexedSeqLike",
"scala.collection.mutable.Iterable$",
"scala.collection.mutable.LazyBuilder",
"scala.collection.mutable.ListBuffer",
"scala.collection.mutable.ListBuffer$",
"scala.collection.mutable.ListBuffer$$anon$1",
"scala.collection.mutable.Map",
"scala.collection.mutable.MapBuilder",
"scala.collection.mutable.MapLike",
"scala.collection.mutable.ResizableArray",
"scala.collection.mutable.Seq",
"scala.collection.mutable.SeqLike",
"scala.collection.mutable.Set",
"scala.collection.mutable.SetBuilder",
"scala.collection.mutable.SetLike",
"scala.collection.mutable.StringBuilder",
"scala.collection.mutable.StringBuilder$",
"scala.collection.mutable.WrappedArray",
"scala.collection.mutable.WrappedArray$",
"scala.collection.mutable.WrappedArray$ofBoolean",
"scala.collection.mutable.WrappedArray$ofByte",
"scala.collection.mutable.WrappedArray$ofChar",
"scala.collection.mutable.WrappedArray$ofDouble",
"scala.collection.mutable.WrappedArray$ofFloat",
"scala.collection.mutable.WrappedArray$ofInt",
"scala.collection.mutable.WrappedArray$ofLong",
"scala.collection.mutable.WrappedArray$ofRef",
"scala.collection.mutable.WrappedArray$ofShort",
"scala.collection.mutable.WrappedArray$ofUnit",
"scala.collection.mutable.WrappedArrayBuilder",
"scala.math.Numeric",
"scala.math.Numeric$IntIsIntegral$",
"scala.math.Ordered",
"scala.math.Ordering",
"scala.math.Ordering$IntOrdering",
"scala.math.ScalaNumber",
"scala.reflect.AnyValManifest",
"scala.reflect.ClassManifest",
"scala.reflect.ClassManifest$",
"scala.reflect.ClassManifest$$anonfun$subargs$1",
"scala.reflect.ClassTypeManifest",
"scala.reflect.Manifest",
"scala.reflect.Manifest$",
"scala.reflect.Manifest$$anon$1",
"scala.reflect.Manifest$$anon$10",
"scala.reflect.Manifest$$anon$11",
"scala.reflect.Manifest$$anon$12",
"scala.reflect.Manifest$$anon$13",
"scala.reflect.Manifest$$anon$14",
"scala.reflect.Manifest$$anon$2",
"scala.reflect.Manifest$$anon$3",
"scala.reflect.Manifest$$anon$4",
"scala.reflect.Manifest$$anon$5",
"scala.reflect.Manifest$$anon$6",
"scala.reflect.Manifest$$anon$7",
"scala.reflect.Manifest$$anon$8",
"scala.reflect.Manifest$$anon$9",
"scala.reflect.Manifest$ClassTypeManifest",
"scala.reflect.NoManifest$",
"scala.runtime.AbstractFunction0",
"scala.runtime.AbstractFunction1",
"scala.runtime.AbstractFunction1$mcII$sp",
"scala.runtime.AbstractFunction2",
"scala.runtime.AbstractFunction3",
"scala.runtime.ArrayRuntime",
"scala.runtime.BooleanRef",
"scala.runtime.BoxedUnit",
"scala.runtime.IntRef",
"scala.runtime.Nothing$",
"scala.runtime.ObjectRef",
"scala.runtime.OrderedProxy",
"scala.runtime.RichInt",
"scala.runtime.ScalaNumberProxy",
"scala.runtime.ScalaRunTime$",
"scala.runtime.VolatileIntRef",
"scala.util.DynamicVariable",
"scala.util.DynamicVariable$$anon$1",
"scala.util.MurmurHash",
"scala.util.MurmurHash$",
"scala.util.MurmurHash$$anonfun$1",
"scala.util.MurmurHash$$anonfun$2",
"scala.util.MurmurHash$$anonfun$symmetricHash$1",
"xsbt.boot.AppConfiguration",
"xsbt.boot.AppID",
"xsbt.boot.AppProperty",
"xsbt.boot.Application",
"xsbt.boot.Application$",
"xsbt.boot.Boot",
"xsbt.boot.Boot$",
"xsbt.boot.Boot$$anonfun$runImpl$1",
"xsbt.boot.BootConfiguration$",
"xsbt.boot.BootException",
"xsbt.boot.BootFilteredLoader",
"xsbt.boot.BootSetup",
"xsbt.boot.Cache$$anonfun$newEntry$1",
"xsbt.boot.CheckProxy$",
"xsbt.boot.Classifiers",
"xsbt.boot.Comment$",
"xsbt.boot.ComponentProvider",
"xsbt.boot.ComponentProvider$",
"xsbt.boot.ComponentProvider$$anonfun$component$1",
"xsbt.boot.Configuration$",
"xsbt.boot.Configuration$$anonfun$1",
"xsbt.boot.Configuration$$anonfun$configurationOnClasspath$1",
"xsbt.boot.Configuration$$anonfun$configurationOnClasspath$2",
"xsbt.boot.Configuration$$anonfun$configurationOnClasspath$3",
"xsbt.boot.Configuration$$anonfun$parse$1",
"xsbt.boot.ConfigurationParser",
"xsbt.boot.ConfigurationParser$",
"xsbt.boot.ConfigurationParser$$anonfun$1",
"xsbt.boot.ConfigurationParser$$anonfun$11",
"xsbt.boot.ConfigurationParser$$anonfun$12",
"xsbt.boot.ConfigurationParser$$anonfun$2",
"xsbt.boot.ConfigurationParser$$anonfun$3",
"xsbt.boot.ConfigurationParser$$anonfun$4",
"xsbt.boot.ConfigurationParser$$anonfun$5",
"xsbt.boot.ConfigurationParser$$anonfun$6",
"xsbt.boot.ConfigurationParser$$anonfun$7",
"xsbt.boot.ConfigurationParser$$anonfun$8",
"xsbt.boot.ConfigurationParser$$anonfun$9",
"xsbt.boot.ConfigurationParser$$anonfun$apply$3",
"xsbt.boot.ConfigurationParser$$anonfun$file$1",
"xsbt.boot.ConfigurationParser$$anonfun$getAppProperties$1",
"xsbt.boot.ConfigurationParser$$anonfun$getAppProperties$2",
"xsbt.boot.ConfigurationParser$$anonfun$getAppProperties$2$$anonfun$10",
"xsbt.boot.ConfigurationParser$$anonfun$getClassifiers$1",
"xsbt.boot.ConfigurationParser$$anonfun$getLevel$1",
"xsbt.boot.ConfigurationParser$$anonfun$getLevel$2",
"xsbt.boot.ConfigurationParser$$anonfun$getLogging$1",
"xsbt.boot.ConfigurationParser$$anonfun$getRepositories$1",
"xsbt.boot.ConfigurationParser$$anonfun$getVersion$1",
"xsbt.boot.ConfigurationParser$$anonfun$ids$1",
"xsbt.boot.ConfigurationParser$$anonfun$parsePropertyDefinition$1",
"xsbt.boot.ConfigurationParser$$anonfun$processClassifiers$1",
"xsbt.boot.ConfigurationParser$$anonfun$processSection$1",
"xsbt.boot.ConfigurationParser$$anonfun$processSection$1$$anonfun$apply$5",
"xsbt.boot.ConfigurationParser$$anonfun$processVersion$1",
"xsbt.boot.ConfigurationParser$$anonfun$readValue$1",
"xsbt.boot.ConfigurationParser$$anonfun$readValue$1$$anonfun$apply$4",
"xsbt.boot.ConfigurationParser$$anonfun$toFiles$1",
"xsbt.boot.ConfigurationParser$$anonfun$trim$1",
"xsbt.boot.Copy$",
"xsbt.boot.Copy$$anonfun$apply$1",
"xsbt.boot.Copy$$anonfun$apply$2",
"xsbt.boot.Copy$$anonfun$apply$2$$anonfun$apply$3",
"xsbt.boot.Enumeration",
"xsbt.boot.Enumeration$$anonfun$1",
"xsbt.boot.Enumeration$$anonfun$members$1",
"xsbt.boot.Enumeration$$anonfun$members$1$$anonfun$apply$1",
"xsbt.boot.Enumeration$$anonfun$members$1$$anonfun$apply$2",
"xsbt.boot.Enumeration$$anonfun$toValue$1",
"xsbt.boot.Enumeration$$anonfun$toValue$2",
"xsbt.boot.Enumeration$Value",
"xsbt.boot.Explicit",
"xsbt.boot.Find",
"xsbt.boot.Find$",
"xsbt.boot.Find$$anonfun$1",
"xsbt.boot.Find$$anonfun$fromRoot$1$1",
"xsbt.boot.Find$$anonfun$fromRoot$1$2",
"xsbt.boot.Find$$anonfun$xsbt$boot$Find$$hasProject$1",
"xsbt.boot.Implicit",
"xsbt.boot.Implicit$$anonfun$1",
"xsbt.boot.Initialize$",
"xsbt.boot.Initialize$$anonfun$1",
"xsbt.boot.Initialize$$anonfun$1$$anonfun$apply$1",
"xsbt.boot.Initialize$$anonfun$1$$anonfun$apply$2",
"xsbt.boot.Initialize$$anonfun$2",
"xsbt.boot.Initialize$$anonfun$initialize$1",
"xsbt.boot.Initialize$$anonfun$process$1",
"xsbt.boot.Initialize$$anonfun$process$2",
"xsbt.boot.Initialize$$anonfun$selectCreate$1",
"xsbt.boot.Initialize$$anonfun$selectFill$1",
"xsbt.boot.Initialize$$anonfun$selectQuick$1",
"xsbt.boot.IvyOptions",
"xsbt.boot.JLine",
"xsbt.boot.JLine$",
"xsbt.boot.JLine$$anonfun$readLine$1",
"xsbt.boot.Labeled",
"xsbt.boot.Launch",
"xsbt.boot.Launch$",
"xsbt.boot.Launch$$anonfun$1",
"xsbt.boot.Launch$$anonfun$explicit$1",
"xsbt.boot.Launch$$anonfun$initialized$1",
"xsbt.boot.Launch$$anonfun$initialized$2",
"xsbt.boot.Launch$$anonfun$xsbt$boot$Launch$$delete$1",
"xsbt.boot.Launch$JNAProvider",
"xsbt.boot.Launch$ScalaProvider",
"xsbt.boot.Launch$ScalaProvider$AppProvider",
"xsbt.boot.Launch$ScalaProvider$AppProvider$$anonfun$baseDirectories$1",
"xsbt.boot.LaunchConfiguration",
"xsbt.boot.Launcher$",
"xsbt.boot.ListMap",
"xsbt.boot.ListMap$",
"xsbt.boot.ListMap$$anon$1",
"xsbt.boot.ListMap$$anon$1$$anonfun$apply$2",
"xsbt.boot.ListMap$$anonfun$apply$1",
"xsbt.boot.ListMap$$anonfun$get$1",
"xsbt.boot.ListMap$$anonfun$get$2",
"xsbt.boot.ListMap$$anonfun$keys$1",
"xsbt.boot.ListMap$$anonfun$xsbt$boot$ListMap$$remove$1",
"xsbt.boot.Loaders$",
"xsbt.boot.Loaders$$anonfun$loaders$1$1",
"xsbt.boot.Locks$",
"xsbt.boot.Locks$$anonfun$1",
"xsbt.boot.Locks$GlobalLock",
"xsbt.boot.Locks$GlobalLock$$anonfun$withFileLock$1",
"xsbt.boot.Locks$InternalLockNPE",
"xsbt.boot.LogLevel$",
"xsbt.boot.Logging",
"xsbt.boot.ParseException",
"xsbt.boot.ParseLine$",
"xsbt.boot.Pre$",
"xsbt.boot.Pre$$anonfun$assert$1",
"xsbt.boot.PromptProperty",
"xsbt.boot.Provider",
"xsbt.boot.Provider$",
"xsbt.boot.Provider$$anonfun$getJars$1",
"xsbt.boot.Provider$$anonfun$getMissing$1",
"xsbt.boot.Provider$$anonfun$toURLs$1",
"xsbt.boot.Provider$JarFilter$",
"xsbt.boot.Provider$initialize",
"xsbt.boot.ProxyProperties$",
"xsbt.boot.Repository$",
"xsbt.boot.Repository$Ivy",
"xsbt.boot.Repository$Maven",
"xsbt.boot.Repository$Predefined",
"xsbt.boot.Repository$Predefined$",
"xsbt.boot.ResolvePaths$",
"xsbt.boot.ResolvePaths$$anonfun$apply$1",
"xsbt.boot.ResolveValues",
"xsbt.boot.ResolveValues$",
"xsbt.boot.ResolveValues$$anonfun$readProperties$1",
"xsbt.boot.ResolveValues$$anonfun$resolve$1",
"xsbt.boot.ResolveValues$$anonfun$resolve$2",
"xsbt.boot.RunConfiguration",
"xsbt.boot.SbtIvyLogger",
"xsbt.boot.SbtIvyLogger$",
"xsbt.boot.SbtMessageLoggerEngine",
"xsbt.boot.Search",
"xsbt.boot.Search$",
"xsbt.boot.Section",
"xsbt.boot.SetProperty",
"xsbt.boot.SimpleReader$",
"xsbt.boot.Update",
"xsbt.boot.Update$$anon$1",
"xsbt.boot.Update$$anon$2",
"xsbt.boot.Update$$anon$3",
"xsbt.boot.Update$$anonfun$1",
"xsbt.boot.Update$$anonfun$2",
"xsbt.boot.Update$$anonfun$3",
"xsbt.boot.Update$$anonfun$4",
"xsbt.boot.Update$$anonfun$addCredentials$1",
"xsbt.boot.Update$$anonfun$addDependency$1",
"xsbt.boot.Update$$anonfun$addDependency$2",
"xsbt.boot.Update$$anonfun$addResolvers$1",
"xsbt.boot.Update$$anonfun$addResolvers$2",
"xsbt.boot.Update$$anonfun$logExceptions$1",
"xsbt.boot.Update$$anonfun$retrieve$1",
"xsbt.boot.Update$$anonfun$settings$1",
"xsbt.boot.Update$$anonfun$xsbt$boot$Update$$addClassifier$1",
"xsbt.boot.Update$$anonfun$xsbt$boot$Update$$hasImplicitClassifier$1",
"xsbt.boot.Update$ArtifactFilter",
"xsbt.boot.UpdateApp",
"xsbt.boot.UpdateConfiguration",
"xsbt.boot.UpdateScala",
"xsbt.boot.UpdateTarget",
"xsbt.boot.Using$",
"xsbt.boot.Value$",
"xsbti.AppConfiguration",
"xsbti.AppMain",
"xsbti.AppProvider",
"xsbti.ApplicationID",
"xsbti.ComponentProvider",
"xsbti.Continue",
"xsbti.Exit",
"xsbti.FullReload",
"xsbti.GlobalLock",
"xsbti.IvyRepository",
"xsbti.Launcher",
"xsbti.MainResult",
"xsbti.MavenRepository",
"xsbti.Predefined",
"xsbti.PredefinedRepository",
"xsbti.Reboot",
"xsbti.Repository",
"xsbti.RetrieveException",
"xsbti.ScalaProvider")

      Logger("play").debug("Loading %s classes".format(toLoad.size))
      for (c <- toLoad) {
        try {
          allClassesCache.add(app.classloader.loadClass(c))
        } catch {
          case t: Throwable => // we don't care
        }
      }
    }
    allClassesCache
  }

  def renderTemplate(name: String, args: Map[String, Any]): Either[Throwable, String] = {

    try {
      val n = System.currentTimeMillis()
      Logger("play").debug("Loading template " + name)
      val template = GenericTemplateLoader.load(name)
      Logger("play").debug("Starting to render")
      val templateArgs = new ConcurrentHashMap[String, AnyRef](args.map(e => (e._1, e._2.asInstanceOf[AnyRef])).asJava)
      val res = template.render(templateArgs)
      Logger("play").debug("Rendered template %s in %s".format(name, System.currentTimeMillis() - n))
      val result = if(Play.isProd && app.configuration.getBoolean("play.groovyTemplates.htmlCompression").getOrElse(true) && name.endsWith("html")) {
        compressor.compress(res)
      } else {
        res
      }

      Right(result)
    } catch {
      case t: Throwable =>
        engine.handleException(t)
        Left(t)
    }

  }

}
