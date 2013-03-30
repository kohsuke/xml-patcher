package org.kohsuke.maven.rewrite;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class XmlVisitor {
    private XmlScanner scanner;

    public XmlScanner getScanner() {
        return scanner;
    }

    public ModifiedPomXMLEventReader getPom() {
        return scanner.getPom();
    }

    /**
     * Called by {@link XmlScanner} at the beginning of scan
     */
    public void startDocument(XmlScanner scanner) {
        this.scanner = scanner;
    }
    public void startElement() {}
    public void endElement() {}

    protected int mark(int i) {
        getPom().mark(i);
        return i;
    }

    protected String getBetween(int m1, int m2) {
        return getPom().getBetween(m1, m2);
    }

    protected XmlPath getPath() {
        return  getScanner().getPath();
    }
}
