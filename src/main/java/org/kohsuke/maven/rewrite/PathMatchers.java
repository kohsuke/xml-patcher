package org.kohsuke.maven.rewrite;

import com.google.common.base.Predicate;

/**
 * @author Kohsuke Kawaguchi
 */
public class PathMatchers {
    public static Predicate<XmlPath> localNames(final String path) {
        return new Predicate<XmlPath>() {
            public boolean apply(XmlPath input) {
                return input!=null && input.getLocalNames().equals(path);
            }
        };
    }
}
