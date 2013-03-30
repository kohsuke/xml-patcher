package org.kohsuke.maven.rewrite;

import org.junit.Test;
import org.kohsuke.maven.rewrite.edit.InsertInElement;

import java.io.File;

public class AppTest {
    @Test
    public void testApp() throws Exception {
        XmlPatcher xml = new XmlPatcher(getClass().getResourceAsStream("/foo.xml"));

        xml.scan(new InsertInElement(PathMatchers.localNames("/project/dependencies")) {
            @Override
            protected String insert() {
                return "<foo>5</foo>";
            }
        });

        if (xml.isModified()) {
            xml.writeTo(new File("test.xml"));
        }
    }
}
