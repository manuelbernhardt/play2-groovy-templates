package play.templates.groovy;

import java.util.HashMap;
import java.util.Map;

public class RenderArgs {

    public Map<String, Object> data = new HashMap<String, Object>();        // ThreadLocal access
    public static ThreadLocal<RenderArgs> current = new ThreadLocal<RenderArgs>() {
        @Override
        protected RenderArgs initialValue() {
            return new RenderArgs();
        }
    };

    public static RenderArgs current() {
        return current.get();
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
