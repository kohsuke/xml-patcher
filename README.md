High-Fidelity XML Patcher
=========================

XML Patcher is a Java library that uses StAX API and provides an API for micro-patching XML file
(such as changing text, adding/removing a tag, etc.) without altering the original XML file unnecessarily
as much as possible. For example, comments, whitespaces between attributes, indentation, and so on are kept intact.

The patching of XML file is primarily done via a handle object called `Mark`, which represents a range of text in XML.
`Mark` can be created from the current location or by combining existing marks (for example by obtaining a range
between two existing marks.

The caller can then retrieve the text that a `Mark` points to, or replace its text by something else.


To simplify the typical edit operations, various common edit commands are provided as a convenience class,
such as the `InsertInElement` class you see below that inserts some stuff inside an existing element.


Example
-------
The following code illustartes the typical usage of the `XmlPatcher` class

        XmlPatcher xml = new XmlPatcher(new File("foo.xml"));

        // insert an <optional> tag in every <dependency> tag
        xml.scan(new InsertElement(PathMatchers.localNames("/project/dependencies/dependency")) {
            protected String insert() {
                return "<optional>true</optional>";
            }
        });

        if (xml.isModified()) {
            xml.writeTo(new File("test.xml"));
        }

Credit
------
The code was originally developed in the Codehaus Mojo project as a part of a Maven plugin.
I extracted it and refactored it since it has nothing to do with Maven at its core.
As such, the code is licensed under Apache Software License 2.0