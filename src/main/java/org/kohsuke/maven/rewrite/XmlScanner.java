package org.kohsuke.maven.rewrite;

import org.codehaus.mojo.versions.rewriting.ModifiedPomXMLEventReader;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * @author Kohsuke Kawaguchi
 */
public class XmlScanner {
    private ModifiedPomXMLEventReader pom;
    private XmlPath path;


    public ModifiedPomXMLEventReader getPom() {
        return pom;
    }

    private void push(StartElement e) {
        path = new XmlPath(path,e);
    }

    private void pop() {
        path = path.getParent();
    }

    public XmlPath getPath() {
        return path;
    }

    public boolean scan(ModifiedPomXMLEventReader pom, XmlVisitor v) throws XMLStreamException {
        if (this.pom!=null)
            throw new IllegalStateException("XmlScanner is not re-entrant");
        this.pom = pom;
        this.path = null;

        try {
            pom.rewind();
            v.startDocument(this);

            while (pom.hasNext()) {
                XMLEvent event = pom.nextEvent();
                if (event.isStartElement()) {
                    push(event.asStartElement());
                    v.startElement();
                }
                if (event.isEndElement()) {
                    v.endElement();
                    pop();
                }
            }
            return pom.isModified();
        } finally {
            this.pom = null;
        }
    }
}
