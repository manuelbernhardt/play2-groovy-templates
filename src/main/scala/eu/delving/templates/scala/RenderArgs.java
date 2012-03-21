package eu.delving.templates.scala;

import play.api.mvc.RequestHeader;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RenderArgs {

    public static Map<RequestHeader, RenderArgs> args = new ConcurrentHashMap<RequestHeader, RenderArgs>();

    public Map<String, Object> data = new HashMap<String, Object>();

    public static RenderArgs renderArgs(RequestHeader requestHeader) {
        RenderArgs arguments = args.get(requestHeader);
        if(arguments == null) {
            arguments = new RenderArgs();
            args.put(requestHeader, arguments);
        }
        return arguments;
    }

    public static void cleanup(RequestHeader requestHeader) {
        args.remove(requestHeader);
    }

    public void put(String key, Object arg) {
        this.data.put(key, arg);
    }

    public Object get(String key) {
        return data.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        return (T) this.get(key);
    }

    @Override
    public String toString() {
        return data.toString();
    }
}
