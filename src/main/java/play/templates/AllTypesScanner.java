package play.templates;

import org.reflections.scanners.AbstractScanner;


/**
 * scans for superclass and interfaces of a class, allowing a reverse lookup for subtypes
 */
public class AllTypesScanner extends AbstractScanner {

    @SuppressWarnings({"unchecked"})
    public void scan(final Object cls) {
        String className = getMetadataAdapter().getClassName(cls);
        getStore().put(className, className);
    }
}
