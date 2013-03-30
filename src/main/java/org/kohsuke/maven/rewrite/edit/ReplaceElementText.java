package org.kohsuke.maven.rewrite.edit;

import com.google.common.base.Predicate;
import org.kohsuke.maven.rewrite.Mark;
import org.kohsuke.maven.rewrite.XmlPath;
import org.kohsuke.maven.rewrite.XmlScanner;
import org.kohsuke.maven.rewrite.XmlVisitor;

/**
 * Replaces an element text, such &lt;foo>abc&lt;/foo> to &lt;foo>def&lt;/foo>
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class ReplaceElementText extends XmlVisitor {
    private final Predicate<XmlPath> matcher;
    private Mark m;

    public ReplaceElementText(Predicate<XmlPath> matcher) {
        this.matcher = matcher;
    }

    @Override
    public void startDocument(XmlScanner scanner) {
        super.startDocument(scanner);
        m = getPatcher().mark();
    }

    @Override
    public void startElement() {
        if (matcher.apply(getPath()))
            m.set();
    }

    @Override
    public void endElement() {
        if (matcher.apply(getPath())) {
            Mark r = m.toCurrent();
            String current = r.verbatim();
            String updated = replace(current);
            r.replace(updated);
            r.clear();
        }
    }

    public abstract String replace(String current);
}
