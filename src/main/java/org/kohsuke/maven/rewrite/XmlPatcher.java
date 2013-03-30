/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.kohsuke.maven.rewrite;

import com.ctc.wstx.stax.WstxInputFactory;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.stax2.XMLInputFactory2;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the modified xml file. Note: implementations of the StAX API (JSR-173) are not good round-trip rewriting
 * <b>while</b> keeping all unchanged bytes in the file as is.  For example, the StAX API specifies that <code>CR</code>
 * characters will be stripped.  Current implementations do not keep &quot; and &apos; characters consistent.
 *
 * @author Stephen Connolly
 * @author Kohsuke Kawaguchi
 */
public class XmlPatcher implements XMLEventReader {

    /**
     * XML being edited.
     */
    /*package*/ final StringBuilder xml;

    private boolean modified = false;

    private XMLInputFactory factory;

    /*package*/ final Set<Mark> marks = new HashSet<Mark>();

    /**
     * Position of the last {@link XMLEvent}
     */
    private final Mark lastPos = new Mark(this);

    /**
     * Position of the upcoming {@link XMLEvent} that we've peaked
     */
    private final Mark nextPos = new Mark(this);

    /**
     * How far does {@link #xml} diverged from the current reading head of {@link #backing}?
     */
    private int cumulativeDelta=0;

    private XMLEvent next = null;

    private XMLEventReader backing;

    private XmlPath path;


// --------------------------- CONSTRUCTORS ---------------------------

    public XmlPatcher(StringBuilder xml) {
        this.xml = xml;
    }

    public XmlPatcher(File f) throws IOException {
        this(new FileInputStream(f));
    }

    public XmlPatcher(InputStream in) throws IOException {
        XmlStreamReader r = new XmlStreamReader(in);
        try {
            this.xml = new StringBuilder(IOUtils.toString(r));
        } finally {
            r.close();
        }
    }

    private static XMLInputFactory2 createDefaultXMLInputFactory() {
        XMLInputFactory2 xif = new WstxInputFactory();
        xif.setProperty(XMLInputFactory2.P_PRESERVE_LOCATION, Boolean.TRUE);
        return xif;
    }

    public void setFactory(XMLInputFactory factory) {
        this.factory = factory;
    }

    /**
     * Rewind to the start so we can run through again.
     *
     * @throws XMLStreamException when things go wrong.
     */
    public void rewind()
            throws XMLStreamException {
        if (factory == null)
            factory = createDefaultXMLInputFactory();
        backing = factory.createXMLEventReader(new StringReader(xml.toString()));
        marks.clear();
        cumulativeDelta = 0;
        nextPos.set(0, 0);
        lastPos.clear();
        next = null;
    }


    /**
     * Gets the current location in the XML document.
     */
    public XmlPath getPath() {
        return path;
    }

    public boolean scan(XmlVisitor v) throws XMLStreamException {
        if (this.path!=null)
            throw new IllegalStateException("XmlScanner is not re-entrant");
        this.path = null;
        rewind();

        try {
            rewind();
            v.startDocument(this);

            while (hasNext()) {
                XMLEvent event = nextEvent();
                if (event.isStartElement()) {
                    path = new XmlPath(path, event.asStartElement());
                    v.startElement();
                }
                if (event.isEndElement()) {
                    v.endElement();
                    path = path.getParent();
                }
            }
            return isModified();
        } finally {
            this.path = null;
        }
    }


    /**
     * Getter for property 'modified'.
     *
     * @return Value for property 'modified'.
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * {@inheritDoc}
     */
    public Object next() {
        try {
            return nextEvent();
        } catch (XMLStreamException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

// --------------------- Interface XMLEventReader ---------------------

    /**
     * {@inheritDoc}
     */
    public XMLEvent nextEvent()
            throws XMLStreamException {
        try {
            return next;
        } finally {
            next = null;
            lastPos.set(nextPos);
        }
    }

    /**
     * {@inheritDoc}
     */
    public XMLEvent peek()
            throws XMLStreamException {
        return backing.peek();
    }

    /**
     * {@inheritDoc}
     */
    public String getElementText()
            throws XMLStreamException {
        return backing.getElementText();
    }

    /**
     * {@inheritDoc}
     */
    public XMLEvent nextTag()
            throws XMLStreamException {
        while (hasNext()) {
            XMLEvent e = nextEvent();
            if (e.isCharacters() && !((Characters) e).isWhiteSpace()) {
                throw new XMLStreamException("Unexpected text");
            }
            if (e.isStartElement() || e.isEndElement()) {
                return e;
            }
        }
        throw new XMLStreamException("Unexpected end of Document");
    }

    /**
     * {@inheritDoc}
     */
    public Object getProperty(String name) {
        return backing.getProperty(name);
    }

    /**
     * {@inheritDoc}
     */
    public void close()
            throws XMLStreamException {
        backing.close();
        next = null;
        backing = null;
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * Returns a copy of the backing string buffer.
     *
     * @return a copy of the backing string buffer.
     */
    public StringBuilder asStringBuilder() {
        return new StringBuilder(xml.toString());
    }

    /**
     * Returns the verbatim text of the element returned by {@link #peek()}.
     *
     * @return the verbatim text of the element returned by {@link #peek()}.
     */
    public String getPeekVerbatim() {
        if (hasNext()) {
            return nextPos.verbatim();
        }
        return "";
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        if (next != null) {
            // fast path
            return true;
        }
        if (!backing.hasNext()) {
            // fast path
            return false;
        }
        try {
            next = backing.nextEvent();
            int s = nextPos.e;
            int e = s;
            if (backing.hasNext()) {
                e = backing.peek().getLocation().getCharacterOffset()+cumulativeDelta;
            }

            if (e != -1) {
                if (!next.isCharacters()) {
                    while (s < e && s < xml.length() &&
                            (c(s) == '\n' || c(s) == '\r')) {
                        s++;
                    }
                    nextPos.set(s, e);
                } else {
                    nextPos.set(s, e);
                    while (nextEndIncludesNextEvent() || nextEndIncludesNextEndElement()) {
                        nextPos.grow(-1);
                    }
                }
            } else {
                nextPos.set(s, e);
            }
            return s < xml.length();
        } catch (XMLStreamException e) {
            return false;
        }
    }

    /**
     * Returns a mark that points to the last returned {@link XMLEvent}
     */
    public Mark getLast() {
        return lastPos;
    }

    /**
     * Creates a new mark.
     */
    public Mark mark() {
        return new Mark(this);
    }

    /**
     * Returns <code>true</code> if nextEnd is including the start of and end element.
     *
     * @return <code>true</code> if nextEnd is including the start of and end element.
     */
    private boolean nextEndIncludesNextEndElement() {
        return nextPos.length() > 2 && c(nextPos.e - 2) == '<';   // ???
    }

    /**
     * Returns <code>true</code> if nextEnd is including the start of the next event.
     *
     * @return <code>true</code> if nextEnd is including the start of the next event.
     */
    private boolean nextEndIncludesNextEvent() {
        return nextPos.length() > 1 && (c(nextPos.e - 1) == '<' || c(nextPos.e - 1) == '&');   // ???
    }

    /**
     * Gets the character at the index provided by the StAX parser.
     *
     * @param index the index.
     * @return char The character.
     */
    private char c(int index) {
        return xml.charAt(index);
    }

    /**
     * Replaces the current element with the replacement text.
     *
     * @param replacement The replacement.
     */
    public void replace(String replacement) {
        lastPos.replace(replacement);
    }

    public String getBetween(Mark a, Mark b) {
        return Mark.between(a, b).verbatim();
    }

    /**
     * Replaces all content between two marks with the replacement text.
     *
     * @param replacement The replacement.
     */
    public void replaceBetween(Mark a, Mark b, String replacement) {
        Mark.between(a, b).replace(replacement);
    }

    /**
     * Update all other marks when one mark has changed its content.
     */
    /*package*/ void updateMarks(Mark changed, int delta) {
        if (delta == 0) return;

        nextPos.update(changed, delta);
        lastPos.update(changed, delta);
        for (Mark m : marks) {
            if (m != changed)
                m.update(changed, delta);
        }
        changed.grow(delta);
        cumulativeDelta+=delta;
        modified = true;
    }

    public void writeTo(File f) throws IOException {
        Writer writer = WriterFactory.newXmlWriter(f);
        try {
            writer.write(xml.toString());
        } finally {
            IOUtil.close(writer);
        }

    }
}
