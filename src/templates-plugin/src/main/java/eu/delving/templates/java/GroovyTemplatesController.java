package eu.delving.templates.java;

import eu.delving.templates.GroovyTemplatesPlugin;
import eu.delving.templates.exceptions.TemplateNotFoundException;
import play.Play;
import play.api.libs.MimeTypes;
import play.i18n.Messages;
import play.libs.Scala;
import play.mvc.Controller;
import play.templates.PlayVirtualFile;
import play.templates.TemplateEngine;
import scala.util.Either;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller to render Groovy Templates
 *
 * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
 */
@SuppressWarnings("unchecked")
public class GroovyTemplatesController extends Controller {

    public static String __LANG = "__LANG";

    public static String __AUTH_TOKEN = "__AUTH_TOKEN";
    
    private static String __RENDER_ARGS = "__RENDER_ARGS";

    public static GroovyTemplateContentBuilder Template(String name) {

        PlayVirtualFile template = TemplateEngine.utils.findTemplateWithPath(name);
        if(!template.exists()) {
            throw new TemplateNotFoundException("Template '%s' not found".format(name));
        }

        scala.Option maybeContentType = MimeTypes.forFileName(template.getName());
        String contentType = "text/html";
        if(maybeContentType.isDefined()) {
            contentType = maybeContentType.get().toString();
        }

        return new GroovyTemplateContentBuilder(name, contentType, renderArgs());
    }
    
    protected static Map<String, Object> renderArgs() {
        Object args = ctx().args.get(__RENDER_ARGS);
        if(args == null) {
            args = new HashMap<String, Object>();
            ctx().args.put(__RENDER_ARGS, args);
        }
        return (Map<String, Object>) args;
    }
    

    public static class GroovyTemplateContentBuilder {

        private final String name;
        private final String contentType;
        private final Map<String, Object> renderArgs;

        Map<String, Object> args = new HashMap<String, Object>();

        public GroovyTemplateContentBuilder(String name, String contentType, Map<String, Object> renderArgs) {
            this.name = name;
            this.contentType = contentType;
            this.renderArgs = renderArgs;
        }

        public GroovyTemplateContent render() {
            return new GroovyTemplateContent(renderGroovyTemplate(name, renderArgs), contentType);
        }

        private String renderGroovyTemplate(final String name, Map<String, Object> renderArgs) {

            Map<String, Object> binding = new HashMap<String, Object>();
            binding.putAll(renderArgs);
            binding.putAll(args);
            binding.put("httpRequest", ctx().request());
            binding.put("request", ctx().args);
            binding.put("session", session());
            binding.put("flash", flash());
            binding.put("params", request().queryString());
            binding.put("messages", new Messages());
            
            String lang = lang().language();
            if(renderArgs.containsKey(GroovyTemplatesController.__LANG)) {
                lang = renderArgs.get(__LANG).toString();
            }

            binding.put("lang", lang);

            scala.collection.immutable.Map<String, Object> map = Scala.asScala(binding);
            Either<Throwable,String> either = Play.application().plugin(GroovyTemplatesPlugin.class).renderTemplate(name, map);
            if(either.isRight()) {
                return either.right().get();
            } else {
                return "";
            }
        }


        public GroovyTemplateContentBuilder params(String k1, Object v1) {
            args.put(k1, v1);
            return this;
        }

        public GroovyTemplateContentBuilder params(String k1, Object v1, String k2, Object v2) {
            args.put(k1, v1);
            args.put(k2, v2);
            return this;
        }

        public GroovyTemplateContentBuilder params(String k1, Object v1, String k2, Object v2, String k3, Object v3) {
            args.put(k1, v1);
            args.put(k2, v2);
            args.put(k3, v3);
            return this;
        }

        public GroovyTemplateContentBuilder params(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4) {
            args.put(k1, v1);
            args.put(k2, v2);
            args.put(k3, v3);
            args.put(k4, v4);
            return this;
        }

        public GroovyTemplateContentBuilder params(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4, String k5, Object v5) {
            args.put(k1, v1);
            args.put(k2, v2);
            args.put(k3, v3);
            args.put(k4, v4);
            args.put(k5, v5);
            return this;
        }

        public GroovyTemplateContentBuilder params(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4, String k5, Object v5, String k6, Object v6) {
            args.put(k1, v1);
            args.put(k2, v2);
            args.put(k3, v3);
            args.put(k4, v4);
            args.put(k5, v5);
            args.put(k6, v6);
            return this;
        }

        public GroovyTemplateContentBuilder params(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4, String k5, Object v5, String k6, Object v6, String k7, Object v7) {
            args.put(k1, v1);
            args.put(k2, v2);
            args.put(k3, v3);
            args.put(k4, v4);
            args.put(k5, v5);
            args.put(k6, v6);
            args.put(k7, v7);
            return this;
        }

        public GroovyTemplateContentBuilder params(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4, String k5, Object v5, String k6, Object v6, String k7, Object v7, String k8, Object v8) {
            args.put(k1, v1);
            args.put(k2, v2);
            args.put(k3, v3);
            args.put(k4, v4);
            args.put(k5, v5);
            args.put(k6, v6);
            args.put(k7, v7);
            args.put(k8, v8);
            return this;
        }

        public GroovyTemplateContentBuilder params(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4, String k5, Object v5, String k6, Object v6, String k7, Object v7, String k8, Object v8, String k9, Object v9) {
            args.put(k1, v1);
            args.put(k2, v2);
            args.put(k3, v3);
            args.put(k4, v4);
            args.put(k5, v5);
            args.put(k6, v6);
            args.put(k7, v7);
            args.put(k8, v8);
            args.put(k9, v9);
            return this;
        }

        public GroovyTemplateContentBuilder params(String k1, Object v1, String k2, Object v2, String k3, Object v3, String k4, Object v4, String k5, Object v5, String k6, Object v6, String k7, Object v7, String k8, Object v8, String k9, Object v9, String k10, Object v10) {
            args.put(k1, v1);
            args.put(k2, v2);
            args.put(k3, v3);
            args.put(k4, v4);
            args.put(k5, v5);
            args.put(k6, v6);
            args.put(k7, v7);
            args.put(k8, v8);
            args.put(k9, v9);
            args.put(k10, v10);
            return this;
        }


    }


}

