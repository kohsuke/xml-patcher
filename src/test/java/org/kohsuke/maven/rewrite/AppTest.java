package org.kohsuke.maven.rewrite;

import org.junit.Test;
import org.kohsuke.maven.rewrite.edit.InsertElement;

import javax.xml.stream.XMLStreamException;
import java.io.File;

public class AppTest {
    @Test
    public void testApp() throws Exception {
        ModifiedPomXMLEventReader newPom = new ModifiedPomXMLEventReader(new File("pom.xml"));

        update(newPom);

        if (newPom.isModified()) {
            newPom.writeTo(new File("test.xml"));
        }
    }

    private boolean update(ModifiedPomXMLEventReader pom) throws XMLStreamException {
        new XmlScanner().scan(pom,new InsertElement(PathMatchers.localNames("/project/dependencies")) {
            @Override
            protected String insert() {
                return "<foo>5</foo>";
            }
        });
        return true;
    }
}
