package org.kohsuke.maven.rewrite;

import javax.xml.stream.events.StartElement;

/**
 * @author Kohsuke Kawaguchi
 */
public class XmlPath {
    private final XmlPath parent;
    private final StartElement tag;
    private final int depth;
    private final String localName;

    XmlPath(XmlPath parent, StartElement tag) {
        this.parent = parent;
        this.tag = tag;

        if (parent==null) {
            this.depth = 0;
            this.localName = '/'+tag.getName().getLocalPart();
        } else {
            this.depth = parent.depth+1;
            this.localName = parent.getLocalNames()+'/'+tag.getName().getLocalPart();
        }
    }

    public XmlPath getParent() {
        return parent;
    }

    public StartElement getTag() {
        return tag;
    }

    /**
     * Takes the current element and return XPath-like path to it by concatanating all the local names.
     *
     * @return
     *      String like '/a/b/c'
     */
    public String getLocalNames() {
        return localName;
    }

    /**
     * Gets depth of this element.
     *
     * The root element has depth 0.
     */
    public int getDepth() {
        return depth;
    }

    @Override
    public String toString() {
        return getLocalNames();
    }
}
