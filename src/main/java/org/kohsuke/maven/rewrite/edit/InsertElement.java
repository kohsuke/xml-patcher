package org.kohsuke.maven.rewrite.edit;

import com.google.common.base.Predicate;
import org.kohsuke.maven.rewrite.XmlPath;
import org.kohsuke.maven.rewrite.XmlScanner;
import org.kohsuke.maven.rewrite.XmlVisitor;

import java.util.HashSet;
import java.util.Set;

/**
 * Insert a new element inside an element content model.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class InsertElement extends XmlVisitor {
    private final Predicate<XmlPath> matcher;

    public InsertElement(Predicate<XmlPath> matcher) {
        this.matcher = matcher;
    }

    private XmlPath lastStart;
    private final Set<XmlPath> rewritten = new HashSet<XmlPath>();

    @Override
    public void startDocument(XmlScanner scanner) {
        super.startDocument(scanner);
        rewritten.clear();
    }

    @Override
    public void startElement() {
        lastStart = getPath();
        XmlPath parent = getPath().getParent();
        if (matcher.apply(parent) && !rewritten.contains(parent)) {
            // insert before this element

            // this captures the indent we want to use after the element is inserted
            doInsert(parent);
        }

        mark(0);
    }

    private void doInsert(XmlPath parent) {
        String indent = getBetween(0, mark(1));
        String inserted = insert();
        if (inserted!=null && inserted.length()!=0) {
            getPom().replaceBetween(0,1,indent+inserted+indent);
            rewritten.add(parent);
        }
    }

    @Override
    public void endElement() {
        if (matcher.apply(getPath()) && lastStart==getPath()) {
            // if we didn't see any element inside where we wanted to rewrite, we come here
            doInsert(getPath());
        }

        mark(0);
    }

    protected abstract String insert();
}
