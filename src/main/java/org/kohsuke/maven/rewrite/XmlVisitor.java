package org.kohsuke.maven.rewrite;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class XmlVisitor {
    private XmlPatcher patcher;

    public XmlPatcher getPatcher() {
        return patcher;
    }

    protected XmlPath getPath() {
        return  getPatcher().getPath();
    }

    /**
     * Called by {@link XmlScanner} at the beginning of scan
     */
    public void startDocument(XmlPatcher patcher) {
        this.patcher = patcher;
    }
    public void startElement() {}
    public void endElement() {}

}
