package org.echosoft.common.io.xml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.echosoft.common.utils.StringUtil;

/**
 * Расширяет возможности стандартного потокового парсера XML, добавляя следующие методы:
 * <ul>
 * <li>{@link #getDepth()} - указывает глубину вложенности текущего элемента дерева.</li>
 * <li>{@link #getAnchorAsText} - указывает положение текущего элемента в дереве в виде XPATH-выражения.</li>
 * <li>{@link #skipTagBody()} - используется для пропуска фрагмента дерева заключенного под текущим тегом.</li>
 * </ul>
 *
 * @author Anton Sharapov
 */
public class XMLStreamReaderExt implements XMLStreamReader {

    private final XMLStreamReader xmlr;
    private final Deque<TagInfo> stack;

    public XMLStreamReaderExt(final XMLStreamReader xmlr) {
        this.xmlr = xmlr;
        this.stack = new ArrayDeque<>(32);
    }

    /**
     * Возвращает структуру, описывающую полный путь от корневого элемента документа до текущего элемента (последнего прочитанного потоком).
     *
     * @return структура с описанием пути от корня до текущего элемента. Никогда не возвращает <code>null</code>.
     */
    public Anchor getAnchor() {
        final Anchor.Part[] parts = new Anchor.Part[stack.size()];
        int i = 0;
        TagInfo parentTag = null;
        for (TagInfo tag : stack) {
            final int order = parentTag != null ? parentTag.children.get(tag.qName) : 1;
            parts[i++] = new Anchor.Part(tag.qName, order);
            parentTag = tag;
        }
        return new Anchor(parts);
    }

    /**
     * Возвращает полный путь до текущего (последнего обработанного) тега дерева от его корня.
     * Данный путь представляет собой простое XPATH-выражение позволяющее найти данный элемент в дереве.
     * пр:  <code>/books/book[137]/author</code>
     *
     * @return XPATH выражение определяющее положение данного элемента в дереве.
     */
    public String getAnchorAsText() {
        final StringBuilder buf = new StringBuilder(32);
        TagInfo parentTag = null;
        for (TagInfo tag : stack) {
            buf.append('/');
            if (!tag.qName.getPrefix().isEmpty())
                buf.append(tag.qName.getPrefix()).append(':');
            buf.append(tag.qName.getLocalPart());
            if (parentTag != null) {
                buf.append('[').append(parentTag.children.get(tag.qName)).append(']');
            } else {
                buf.append("[1]");
            }
            parentTag = tag;
        }
        return buf.length() > 0 ? buf.toString() : "/";
    }

    /**
     * Метод возвращает глубину вложенности текущего (последнего обработанного) тега дерева.
     *
     * @return глубина вложенности текущего тега дерева XML.
     */
    public int getDepth() {
        return stack.size();
    }

    /**
     * Возвращает информацию об элементе, на котором в настоящий момент спозиционирован поток.
     *
     * @return информация о текущем элементе (полное имя, атрибуты, сопоставленные с ним пространства имен).
     * @throws IllegalStateException в случае если входной поток находится в состоянии отличном от START_ELEMENT или END_ELEMENT.
     */
    public Element getElement() {
        return new Element(xmlr);
    }

    /**
     * Метод пропускает тело текущего тега со всем его вложенным содержимым.<br/>
     * <strong>Предусловие:</strong> Курсор потока должен указывать на открывающий элемент тега чье содержимое требуется пропустить.
     * Если данное условие не выполняется то метод поднимет исключение.</br/>
     * <strong>Постусловие:</strong> По завершении метода курсор будет указывать на закрывающий элемент данного тега.
     *
     * @return тип текщуего узла (всегда {@link XMLStreamConstants#END_ELEMENT}).
     * @throws NoSuchElementException в случае преждевременного завершения читаемого документа XML.
     * @throws XMLStreamException     поднимается в одном из трех случаев:
     *   <ol>
     *     <li>в случае если при вызове данного метода курсор потока не указывает на открывающий элемент тега</li>
     *     <li>в случае если при завершении обработки данного метода курсор потока не был спозиционирован на закрывающий элемент тега</li>
     *     <li>если в процессе итерации по содержимому данного тега возникла какая-либо непредвиденная ошибка связанная с невалидной структурой документа.</li>
     *   </ol>
     */
    public int skipTagBody() throws XMLStreamException, NoSuchElementException {
        xmlr.require(START_ELEMENT, null, null);
        int delta = 1, eventType = 0;
        while (delta > 0) {
            eventType = xmlr.next();
            switch (eventType) {
                case START_ELEMENT:
                    delta++;
                    break;
                case END_ELEMENT:
                    delta--;
                    break;
                default:
                    break;
            }
        }
        xmlr.require(END_ELEMENT, null, null);
        return eventType;
    }

    /**
     * Метод читает содержимое данного элемента и сериализует его в указанный в аргументе выходной поток.<br/>
     * <strong>Предусловие:</strong> Курсор потока должен указывать на открывающий элемент тега чье содержимое требуется сериализовать.
     * Если данное условие не выполняется то метод поднимет исключение.</br/>
     * <strong>Постусловие:</strong> По завершении метода курсор будет указывать на закрывающий элемент данного тега.
     *
     * @param xmlw поток куда будет осуществляться запись содержимого данного элемента.
     * @throws NoSuchElementException в случае преждевременного завершения читаемого документа XML.
     * @throws XMLStreamException     поднимается в одном из трех случаев:
     *   <ol>
     *     <li>в случае если при вызове данного метода курсор потока не указывает на открывающий элемент тега</li>
     *     <li>в случае если при завершении обработки данного метода курсор потока не был спозиционирован на закрывающий элемент тега</li>
     *     <li>если в процессе итерации по содержимому данного тега возникла какая-либо непредвиденная ошибка связанная с невалидной структурой документа.</li>
     *   </ol>
     */
    public void serializeTag(final XMLStreamWriter xmlw) throws XMLStreamException {
        xmlr.require(START_ELEMENT, null, null);
        serializeStartElement(xmlw);
        int delta = 1;
        while (true) {
            switch (xmlr.next()) {
                case START_ELEMENT:
                    serializeStartElement(xmlw);
                    delta++;
                    break;
                case END_ELEMENT:
                    xmlw.writeEndElement();
                    delta--;
                    if (delta == 0) {
                        return;
                    }
                    break;
                case PROCESSING_INSTRUCTION:
                    xmlw.writeProcessingInstruction(xmlr.getPITarget(), xmlr.getPIData());
                    break;
                case SPACE:
                case CHARACTERS:
                    xmlw.writeCharacters(xmlr.getText());
                    break;
                case COMMENT:
                    xmlw.writeComment(xmlr.getText());
                    break;
                case ENTITY_REFERENCE:
                    xmlw.writeEntityRef(xmlr.getLocalName());
                    break;
                case DTD:
                    xmlw.writeDTD(xmlr.getText());
                    break;
                case CDATA:
                    xmlw.writeCData(xmlr.getText());
                    break;
                case ATTRIBUTE:             // вся работа уже выполнена при обработке события START_ELEMENT.
                case NAMESPACE:             // вся работа уже выполнена при обработке события START_ELEMENT.
                case NOTATION_DECLARATION:
                case ENTITY_DECLARATION:
                default:
                    break;
            }
        }
    }

    /**
     * Метод читает внутреннее содержимое текущего элемента (не включая его открывающие и закрывающие теги) и сериализует его в указанный в аргументе выходной поток.<br/>
     * <strong>Предусловие:</strong> Курсор потока должен указывать на открывающий элемент тега чье содержимое требуется сериализовать.
     * Если данное условие не выполняется то метод поднимет исключение.</br/>
     * <strong>Постусловие:</strong> По завершении метода курсор будет указывать на закрывающий элемент данного тега.
     *
     * @param xmlw поток куда будет осуществляться запись содержимого данного элемента.
     * @throws NoSuchElementException в случае преждевременного завершения читаемого документа XML.
     * @throws XMLStreamException  поднимается в одном из трех случаев:
     *   <ol>
     *     <li>в случае если при вызове данного метода курсор потока не указывает на открывающий элемент тега</li>
     *     <li>в случае если при завершении обработки данного метода курсор потока не был спозиционирован на закрывающий элемент тега</li>
     *     <li>если в процессе итерации по содержимому данного тега возникла какая-либо непредвиденная ошибка связанная с невалидной структурой документа.</li>
     *   </ol>
     */
    public void serializeTagBody(final XMLStreamWriter xmlw) throws XMLStreamException {
        xmlr.require(START_ELEMENT, null, null);
        int delta = 1;
        while (true) {
            switch (xmlr.next()) {
                case START_ELEMENT:
                    serializeStartElement(xmlw);
                    delta++;
                    break;
                case END_ELEMENT:
                    delta--;
                    if (delta == 0) {
                        return;
                    }
                    xmlw.writeEndElement();
                    break;
                case PROCESSING_INSTRUCTION:
                    xmlw.writeProcessingInstruction(xmlr.getPITarget(), xmlr.getPIData());
                    break;
                case SPACE:
                case CHARACTERS:
                    xmlw.writeCharacters(xmlr.getText());
                    break;
                case COMMENT:
                    xmlw.writeComment(xmlr.getText());
                    break;
                case ENTITY_REFERENCE:
                    xmlw.writeEntityRef(xmlr.getLocalName());
                    break;
                case DTD:
                    xmlw.writeDTD(xmlr.getText());
                    break;
                case CDATA:
                    xmlw.writeCData(xmlr.getText());
                    break;
                case ATTRIBUTE:             // вся работа уже выполнена при обработке события START_ELEMENT.
                case NAMESPACE:             // вся работа уже выполнена при обработке события START_ELEMENT.
                case NOTATION_DECLARATION:
                case ENTITY_DECLARATION:
                default:
                    break;
            }
        }
    }

    protected void serializeStartElement(final XMLStreamWriter xmlw) throws XMLStreamException {
        QName name = xmlr.getName();
        xmlw.writeStartElement(name.getPrefix(), name.getLocalPart(), name.getNamespaceURI());
        for (int i = 0, cnt = xmlr.getNamespaceCount(); i < cnt; i++) {
            xmlw.writeNamespace(xmlr.getNamespacePrefix(i), xmlr.getNamespaceURI(i));
        }
        for (int i = 0, cnt = xmlr.getAttributeCount(); i < cnt; i++) {
            name = xmlr.getAttributeName(i);
            xmlw.writeAttribute(name.getPrefix(), name.getNamespaceURI(), name.getLocalPart(), xmlr.getAttributeValue(i));
        }
    }

    protected void serializeStartElement(final Writer out) throws IOException {
        QName name = xmlr.getName();
        out.write('<');
        if (!name.getPrefix().isEmpty()) {
            out.append(name.getPrefix()).write(':');
        }
        out.write(name.getLocalPart());

        for (int i = 0, cnt = xmlr.getNamespaceCount(); i < cnt; i++) {
            final String prefix = xmlr.getNamespacePrefix(i);
            out.write(" xmlns");
            if (prefix != null && prefix.length() > 0 && !XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
                out.append(':').write(prefix);
            }
            out.write("=\"");
            StringUtil.encodeXMLAttribute(out, xmlr.getNamespaceURI(i));
            out.write('\"');
        }

        for (int i = 0, cnt = xmlr.getAttributeCount(); i < cnt; i++) {
            name = xmlr.getAttributeName(i);
            out.write(' ');
            if (!name.getPrefix().isEmpty()) {
                out.append(name.getPrefix()).write(':');
            }
            out.append(name.getLocalPart()).write("=\"");
            StringUtil.encodeXMLAttribute(out, xmlr.getAttributeValue(i));
            out.write('\"');
        }
        out.write('>');
    }

    protected void serializeEndElement(final Writer out) throws IOException {
        QName name = xmlr.getName();
        out.write("</");
        if (!name.getPrefix().isEmpty()) {
            out.append(name.getPrefix()).write(':');
        }
        out.append(name.getLocalPart()).write('>');
    }

    @Override
    public int next() throws XMLStreamException {
        if (xmlr.getEventType() == END_ELEMENT) {
            stack.removeLast();
        }
        final int eventType = xmlr.next();
        if (eventType == START_ELEMENT) {
            final TagInfo parent = stack.peekLast();
            if (parent != null)
                parent.addChild(xmlr.getName());
            stack.addLast(new TagInfo(xmlr.getName()));
        }
        return eventType;
    }

    @Override
    public int nextTag() throws XMLStreamException {
        while (true) {
            final int eventType = next();
            switch (eventType) {
                case SPACE:
                case COMMENT:
                case PROCESSING_INSTRUCTION:
                    continue;
                case CDATA:
                case CHARACTERS:
                    if (isWhiteSpace()) {
                        continue;
                    }
                    throw new XMLStreamException("Received non-all-whitespace CHARACTERS or CDATA event in nextTag().", getLocation());
                case START_ELEMENT:
                case END_ELEMENT:
                    return eventType;
            }
            throw new XMLStreamException("found: " + getEventTypeString(eventType) + ", expected " + getEventTypeString(XMLStreamConstants.START_ELEMENT) + " or " + getEventTypeString(XMLStreamConstants.END_ELEMENT), getLocation());
        }
    }

    @Override
    public Object getProperty(final String name) throws IllegalArgumentException {
        return xmlr.getProperty(name);
    }

    @Override
    public void require(final int type, final String namespaceURI, final String localName) throws XMLStreamException {
        xmlr.require(type, namespaceURI, localName);
    }

    @Override
    public String getElementText() throws XMLStreamException {
        return xmlr.getElementText();
    }

    @Override
    public boolean hasNext() throws XMLStreamException {
        return xmlr.hasNext();
    }

    @Override
    public void close() throws XMLStreamException {
        xmlr.close();
    }

    @Override
    public String getNamespaceURI(final String prefix) {
        return xmlr.getNamespaceURI(prefix);
    }

    @Override
    public boolean isStartElement() {
        return xmlr.isStartElement();
    }

    @Override
    public boolean isEndElement() {
        return xmlr.isEndElement();
    }

    @Override
    public boolean isCharacters() {
        return xmlr.isCharacters();
    }

    @Override
    public boolean isWhiteSpace() {
        return xmlr.isWhiteSpace();
    }

    @Override
    public String getAttributeValue(final String namespaceURI, final String localName) {
        return xmlr.getAttributeValue(namespaceURI, localName);
    }

    @Override
    public int getAttributeCount() {
        return xmlr.getAttributeCount();
    }

    @Override
    public QName getAttributeName(final int index) {
        return xmlr.getAttributeName(index);
    }

    @Override
    public String getAttributeNamespace(final int index) {
        return xmlr.getAttributeNamespace(index);
    }

    @Override
    public String getAttributeLocalName(final int index) {
        return xmlr.getAttributeLocalName(index);
    }

    @Override
    public String getAttributePrefix(final int index) {
        return xmlr.getAttributePrefix(index);
    }

    @Override
    public String getAttributeType(final int index) {
        return xmlr.getAttributeType(index);
    }

    @Override
    public String getAttributeValue(final int index) {
        return xmlr.getAttributeValue(index);
    }

    @Override
    public boolean isAttributeSpecified(final int index) {
        return xmlr.isAttributeSpecified(index);
    }

    @Override
    public int getNamespaceCount() {
        return xmlr.getNamespaceCount();
    }

    @Override
    public String getNamespacePrefix(final int index) {
        return xmlr.getNamespacePrefix(index);
    }

    @Override
    public String getNamespaceURI(final int index) {
        return xmlr.getNamespaceURI(index);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return xmlr.getNamespaceContext();
    }

    @Override
    public int getEventType() {
        return xmlr.getEventType();
    }

    @Override
    public String getText() {
        return xmlr.getText();
    }

    @Override
    public char[] getTextCharacters() {
        return xmlr.getTextCharacters();
    }

    @Override
    public int getTextCharacters(final int sourceStart, final char[] target, final int targetStart, final int length) throws XMLStreamException {
        return xmlr.getTextCharacters(sourceStart, target, targetStart, length);
    }

    @Override
    public int getTextStart() {
        return xmlr.getTextStart();
    }

    @Override
    public int getTextLength() {
        return xmlr.getTextLength();
    }

    @Override
    public String getEncoding() {
        return xmlr.getEncoding();
    }

    @Override
    public boolean hasText() {
        return xmlr.hasText();
    }

    @Override
    public Location getLocation() {
        return xmlr.getLocation();
    }

    @Override
    public QName getName() {
        return xmlr.getName();
    }

    @Override
    public String getLocalName() {
        return xmlr.getLocalName();
    }

    @Override
    public boolean hasName() {
        return xmlr.hasName();
    }

    @Override
    public String getNamespaceURI() {
        return xmlr.getNamespaceURI();
    }

    @Override
    public String getPrefix() {
        return xmlr.getPrefix();
    }

    @Override
    public String getVersion() {
        return xmlr.getVersion();
    }

    @Override
    public boolean isStandalone() {
        return xmlr.isStandalone();
    }

    @Override
    public boolean standaloneSet() {
        return xmlr.standaloneSet();
    }

    @Override
    public String getCharacterEncodingScheme() {
        return xmlr.getCharacterEncodingScheme();
    }

    @Override
    public String getPITarget() {
        return xmlr.getPITarget();
    }

    @Override
    public String getPIData() {
        return xmlr.getPIData();
    }


    private static String getEventTypeString(final int eventType) {
        switch (eventType) {
            case START_ELEMENT:
                return "START_ELEMENT";
            case END_ELEMENT:
                return "END_ELEMENT";
            case PROCESSING_INSTRUCTION:
                return "PROCESSING_INSTRUCTION";
            case CHARACTERS:
                return "CHARACTERS";
            case COMMENT:
                return "COMMENT";
            case START_DOCUMENT:
                return "START_DOCUMENT";
            case END_DOCUMENT:
                return "END_DOCUMENT";
            case ENTITY_REFERENCE:
                return "ENTITY_REFERENCE";
            case ATTRIBUTE:
                return "ATTRIBUTE";
            case DTD:
                return "DTD";
            case CDATA:
                return "CDATA";
            case SPACE:
                return "SPACE";
            default:
                return "UNKNOWN_EVENT_TYPE( " + String.valueOf(eventType) + ")";
        }
    }


    private static final class TagInfo {
        private final QName qName;
        private final Map<QName, Integer> children;

        private TagInfo(final QName qName) {
            this.qName = qName;
            this.children = new HashMap<>();
        }

        private void addChild(final QName cn) {
            final Integer count = children.get(cn);
            children.put(cn, count == null ? 1 : count + 1);
        }

        @Override
        public String toString() {
            final StringBuilder buf = new StringBuilder(50);
            buf.append("[TagInfo{name:");
            if (!XMLConstants.DEFAULT_NS_PREFIX.equals(qName.getPrefix())) {
                buf.append(qName.getPrefix()).append(':');
            }
            buf.append(qName.getLocalPart());
            buf.append(", children:");
            buf.append(children);
            buf.append("}]");
            return buf.toString();
        }
    }
}
