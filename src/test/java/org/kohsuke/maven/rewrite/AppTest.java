package org.kohsuke.maven.rewrite;

import com.ctc.wstx.stax.WstxInputFactory;
import org.apache.commons.io.FileUtils;
import org.codehaus.mojo.versions.api.PomHelper;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.stax2.XMLInputFactory2;
import org.junit.Test;
import org.kohsuke.maven.rewrite.edit.InsertElement;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

public class AppTest {
    @Test
    public void testApp() throws Exception {
        StringBuilder input = FileUtils.readFileToString(new File("pom.xml"));
        ModifiedPomXMLEventReader newPom = new ModifiedPomXMLEventReader( input );

        update( newPom );

        if (newPom.isModified()) {
            writeFile(new File("test.xml"), input);
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

    /**
     * Writes a StringBuilder into a file.
     *
     * @param outFile The file to read.
     * @param input   The contents of the file.
     * @throws IOException when things go wrong.
     */
    protected final void writeFile(File outFile, StringBuilder input)
            throws IOException {
        Writer writer = WriterFactory.newXmlWriter(outFile);
        try {
            IOUtil.copy(input.toString(), writer);
        } finally {
            IOUtil.close(writer);
        }
    }
}
