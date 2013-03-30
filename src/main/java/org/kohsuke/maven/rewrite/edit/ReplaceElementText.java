package org.kohsuke.maven.rewrite.edit;

import com.google.common.base.Predicate;
import org.kohsuke.maven.rewrite.XmlPath;
import org.kohsuke.maven.rewrite.XmlVisitor;

/**
 * Replaces an element text, such &lt;foo>abc&lt;/foo> to &lt;foo>def&lt;/foo>
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class ReplaceElementText extends XmlVisitor {
    private final Predicate<XmlPath> matcher;

    public ReplaceElementText(Predicate<XmlPath> matcher) {
        this.matcher = matcher;
    }

    @Override
    public void startElement() {
        if (matcher.apply(getPath()))
            mark(0);
    }

    @Override
    public void endElement() {
        if (matcher.apply(getPath())) {
            String current = getBetween(0, mark(1));
            String updated = replace(current);
            getPom().replaceBetween(0, 1, updated);
        }
    }

    public abstract String replace(String current);
}
