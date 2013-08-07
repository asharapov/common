package org.echosoft.common.io.xml;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.echosoft.common.utils.StringUtil;

/**
 * Содержит сведения об отдельно взятом элементе XML документа и средства для сериализации информации об этом элементе в поток.
 *
 * @author Anton Sharapov
 */
public class Element implements Serializable, Cloneable {

    private final QName name;
    private List<NameSpace> namespaces;
    private List<Attribute> attributes;

    /**
     * Создает объект со сведениями о текущем элементе XML документа взятом из входного потока.
     *
     * @param xmlr входной поток, спозиционированный на теге элемента.
     * @throws IllegalStateException в случае если входной поток находится в состоянии отличном от START_ELEMENT или END_ELEMENT.
     */
    public Element(final XMLStreamReader xmlr) {
        this.name = xmlr.getName();
        int cnt = xmlr.getNamespaceCount();
        if (cnt > 0) {
            namespaces = new ArrayList<NameSpace>(cnt);
            for (int i = 0; i < cnt; i++)
                namespaces.add(new NameSpace(xmlr.getNamespacePrefix(i), xmlr.getNamespaceURI(i)));
        } else {
            namespaces = null;
        }
        cnt = xmlr.getAttributeCount();
        if (cnt > 0) {
            attributes = new ArrayList<Attribute>(cnt);
            for (int i = 0; i < cnt; i++) {
                attributes.add(new Attribute(xmlr.getAttributeName(i), xmlr.getAttributeValue(i)));
            }
        } else {
            attributes = null;
        }
    }

    private Element(final QName name) {
        this.name = name;
    }

    public QName getName() {
        return name;
    }

    public int getAttributesCount() {
        return attributes != null ? attributes.size() : 0;
    }

    public Attribute getAttribute(final int i) {
        if (attributes == null || attributes.size() >= i || i < 0)
            return null;
        return attributes.get(i);
    }

    public String getAttribute(final QName qName) {
        if (attributes != null) {
            for (Attribute attr : attributes) {
                if (attr.name.equals(qName))
                    return attr.value;
            }
        }
        return null;
    }
    public void setAttribute(final QName qName, final String value) {
        if (attributes == null) {
            if (value != null) {
                attributes = new ArrayList<Attribute>(2);
                attributes.add(new Attribute(qName, value));
            }
        } else {
            for (int i = attributes.size() - 1; i >= 0; i--) {
                if (attributes.get(i).name.equals(qName)) {
                    if (value != null) {
                        attributes.set(i, new Attribute(qName, value));
                    } else {
                        attributes.remove(i);
                    }
                    return;
                }
            }
            attributes.add(new Attribute(qName, value));
        }
    }

    public String getAttribute(final String localName) {
        if (attributes != null) {
            for (Attribute attr : attributes) {
                if (attr.name.getLocalPart().equals(localName))
                    return attr.value;
            }
        }
        return null;
    }
    public void setAttribute(final String localName, final String value) {
        if (attributes == null) {
            if (value != null) {
                attributes = new ArrayList<Attribute>(2);
                attributes.add(new Attribute(new QName(localName), value));
            }
        } else {
            for (int i = attributes.size() - 1; i >= 0; i--) {
                if (attributes.get(i).name.getLocalPart().equals(localName)) {
                    if (value != null) {
                        attributes.set(i, new Attribute(new QName(localName), value));
                    } else {
                        attributes.remove(i);
                    }
                    return;
                }
            }
            attributes.add(new Attribute(new QName(localName), value));
        }
    }


    public void writeOpenTag(final StringBuilder buf) {
        buf.append('<');
        if (!name.getPrefix().isEmpty()) {
            buf.append(name.getPrefix()).append(':');
        }
        buf.append(name.getLocalPart());

        if (namespaces != null) {
            for (NameSpace ns : namespaces) {
                buf.append(" xmlns");
                final String prefix = ns.getPrefix();
                if (prefix != null && prefix.length() > 0 && !XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
                    buf.append(':').append(prefix);
                }
                buf.append('=').append('\"');
                StringUtil.encodeXMLAttribute(buf, ns.getURI());
                buf.append('\"');
            }
        }

        if (attributes != null) {
            for (Attribute attr : attributes) {
                buf.append(' ');
                final QName name = attr.getName();
                if (!name.getPrefix().isEmpty()) {
                    buf.append(name.getPrefix()).append(':');
                }
                buf.append(name.getLocalPart()).append('=').append('\"');
                StringUtil.encodeXMLAttribute(buf, attr.getValue());
                buf.append('\"');
            }
        }
        buf.append('>');
    }

    public void writeOpenTag(final Writer out) throws IOException {
        out.write('<');
        if (!name.getPrefix().isEmpty()) {
            out.append(name.getPrefix()).write(':');
        }
        out.write(name.getLocalPart());

        if (namespaces != null) {
            for (NameSpace ns : namespaces) {
                out.write(" xmlns");
                final String prefix = ns.getPrefix();
                if (prefix != null && prefix.length() > 0 && !XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
                    out.append(':').write(prefix);
                }
                out.write("=\"");
                StringUtil.encodeXMLAttribute(out, ns.getURI());
                out.write('\"');
            }
        }

        if (attributes != null) {
            for (Attribute attr : attributes) {
                out.write(' ');
                final QName name = attr.getName();
                if (!name.getPrefix().isEmpty()) {
                    out.append(name.getPrefix()).write(':');
                }
                out.append(name.getLocalPart()).write("=\"");
                StringUtil.encodeXMLAttribute(out, attr.getValue());
                out.write('\"');
            }
        }
        out.write('>');
    }

    public void writeOpenTag(final XMLStreamWriter xmlw) throws XMLStreamException {
        xmlw.writeStartElement(name.getPrefix(), name.getLocalPart(), name.getNamespaceURI());
        if (namespaces != null) {
            for (NameSpace ns : namespaces) {
                xmlw.writeNamespace(ns.getPrefix(), ns.getURI());
            }
        }
        if (attributes != null) {
            for (Attribute attr : attributes) {
                final QName name = attr.getName();
                xmlw.writeAttribute(name.getPrefix(), name.getNamespaceURI(), name.getLocalPart(), attr.getValue());
            }
        }
    }

    public void writeCloseTag(final StringBuilder buf) {
        buf.append("</");
        if (!name.getPrefix().isEmpty()) {
            buf.append(name.getPrefix()).append(':');
        }
        buf.append(name.getLocalPart()).append('>');
    }

    public void writeCloseTag(final Writer out) throws IOException {
        out.write("</");
        if (!name.getPrefix().isEmpty()) {
            out.append(name.getPrefix()).write(':');
        }
        out.append(name.getLocalPart()).write('>');
    }

    public void writeCloseTag(final XMLStreamWriter xmlw) throws XMLStreamException {
        xmlw.writeEndElement();
    }

    public Element cloneElement() {
        final Element result = new Element(name);
        if (this.namespaces != null) {
            result.namespaces = new ArrayList<NameSpace>(this.namespaces);
        }
        if (this.attributes != null) {
            result.attributes = new ArrayList<Attribute>(this.attributes);
        }
        return result;
    }

    @Override
    public Element clone() throws CloneNotSupportedException {
        final Element result = (Element)super.clone();
        if (this.namespaces != null) {
            result.namespaces = new ArrayList<NameSpace>(this.namespaces);
        }
        if (this.attributes != null) {
            result.attributes = new ArrayList<Attribute>(this.attributes);
        }
        return result;
    }


    public static class NameSpace implements Serializable, Cloneable {
        private final String prefix;
        private final String nsURI;

        public NameSpace(final String prefix, final String nsURI) {
            this.prefix = prefix;
            this.nsURI = nsURI;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getURI() {
            return nsURI;
        }

        @Override
        public NameSpace clone() throws CloneNotSupportedException {
            return (NameSpace)super.clone();
        }
    }


    public static class Attribute implements Serializable, Cloneable {
        private final QName name;
        private final String value;

        public Attribute(final QName name, final String value) {
            this.name = name;
            this.value = value;
        }

        public QName getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        @Override
        public Attribute clone() throws CloneNotSupportedException {
            return (Attribute)super.clone();
        }
    }
}
