package org.kohsuke.maven.rewrite;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class XmlVisitor {
    private XmlScanner scanner;

    public XmlScanner getScanner() {
        return scanner;
    }

    public XmlPatcher getPatcher() {
        return scanner.getPom();
    }

    protected XmlPath getPath() {
        return  getScanner().getPath();
    }

    /**
     * Called by {@link XmlScanner} at the beginning of scan
     */
    public void startDocument(XmlScanner scanner) {
        this.scanner = scanner;
    }
    public void startElement() {}
    public void endElement() {}

}
