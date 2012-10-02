package org.echosoft.common.io.xml;

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

    private final XMLStreamReader proxy;
    private final Deque<TagInfo> stack;

    public XMLStreamReaderExt(final XMLStreamReader proxy) {
        this.proxy = proxy;
        this.stack = new ArrayDeque<TagInfo>(32);
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
     * Метод пропускает тело текущего тега со всем его вложенным содержимым.<br/>
     * <strong>Предусловие:</strong> Курсор потока должен указывать на открывающий элемент тега чье содержимое требуется пропустить.
     * Если данное условие не выполняется то метод поднимет исключение.</br/>
     * <strong>Постусловие:</strong> По завершении метода курсор будет указывать на закрывающий элемент данного тега.
     *
     * @return тип текщуего узла (всегда {@link XMLStreamConstants#END_ELEMENT}).
     * @throws NoSuchElementException в случае преждевременного завершения читаемого документа XML.
     * @throws XMLStreamException поднимается в одном из трех случаев:
     *   <ol>
     *     <li>в случае если при вызове данного метода курсор потока не указывает на открывающий элемент тега</li>
     *     <li>в случае если при завершении обработки данного метода курсор потока не был спозиционирован на закрывающий элемент тега</li>
     *     <li>если в процессе итерации по содержимому данного тега возникла какая-либо непредвиденная ошибка связанная с невалидной структурой документа.</li>
     *   </ol>
     */
    public int skipTagBody() throws XMLStreamException, NoSuchElementException {
        proxy.require(START_ELEMENT, null, null);
        int delta = 1, eventType = 0;
        while (delta > 0) {
            eventType = proxy.next();
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
        proxy.require(END_ELEMENT, null, null);
        return eventType;
    }

    /**
     * Метод читает содержимое данного тега и сериализует его в указанный в аргументе поток.<br/>
     * <strong>Предусловие:</strong> Курсор потока должен указывать на открывающий элемент тега чье содержимое требуется пропустить.
     * Если данное условие не выполняется то метод поднимет исключение.</br/>
     * <strong>Постусловие:</strong> По завершении метода курсор будет указывать на закрывающий элемент данного тега.
     *
     * @param writer поток куда будет осуществляться запись содержимого данного тега.
     * @return тип текщуего узла (всегда {@link XMLStreamConstants#END_ELEMENT}).
     * @throws NoSuchElementException в случае преждевременного завершения читаемого документа XML.
     * @throws XMLStreamException поднимается в одном из трех случаев:
     *   <ol>
     *     <li>в случае если при вызове данного метода курсор потока не указывает на открывающий элемент тега</li>
     *     <li>в случае если при завершении обработки данного метода курсор потока не был спозиционирован на закрывающий элемент тега</li>
     *     <li>если в процессе итерации по содержимому данного тега возникла какая-либо непредвиденная ошибка связанная с невалидной структурой документа.</li>
     *   </ol>
     */
    public int serializeTag(final XMLStreamWriter writer) throws XMLStreamException {
        proxy.require(START_ELEMENT, null, null);
        serializeStartElement(writer);
        int delta = 1, eventType = 0;
        while (delta > 0) {
            eventType = proxy.next();
            switch (eventType) {
                case START_ELEMENT:
                    delta++;
                    serializeStartElement(writer);
                    break;
                case END_ELEMENT:
                    delta--;
                    writer.writeEndElement();
                    break;
                case PROCESSING_INSTRUCTION:
                    writer.writeProcessingInstruction(proxy.getPITarget(), proxy.getPIData());
                    break;
                case SPACE:
                case CHARACTERS:
                    writer.writeCharacters(proxy.getText());
                    break;
                case COMMENT:
                    writer.writeComment(proxy.getText());
                    break;
                case ENTITY_REFERENCE:
                    writer.writeEntityRef(proxy.getLocalName());
                    break;
                case DTD:
                    writer.writeDTD(proxy.getText());
                    break;
                case CDATA:
                    writer.writeCData(proxy.getText());
                    break;
                case ATTRIBUTE:             // вся работа уже выполнена при обработке события START_ELEMENT.
                case NAMESPACE:             // вся работа уже выполнена при обработке события START_ELEMENT.
                case NOTATION_DECLARATION:
                case ENTITY_DECLARATION:
                default:
                    break;
            }
        }
        proxy.require(END_ELEMENT, null, null);
        return eventType;
    }
    private void serializeStartElement(final XMLStreamWriter writer) throws XMLStreamException {
        final String uri = proxy.getNamespaceURI();
        if (uri != null) {
            final String prefix = proxy.getPrefix();
            if (prefix != null) {
                writer.writeStartElement(prefix, proxy.getLocalName(), uri);
            } else {
                writer.writeStartElement(uri, proxy.getLocalName());
            }
        } else {
            writer.writeStartElement(proxy.getLocalName());
        }
        for (int i = 0, len = proxy.getNamespaceCount(); i < len; i++) {
            writer.writeNamespace(proxy.getNamespacePrefix(i), proxy.getNamespaceURI(i));
        }
        for (int i = 0, len = proxy.getAttributeCount(); i < len; i++) {
            final String attrUri = proxy.getAttributeNamespace(i);
            if (attrUri != null) {
                writer.writeAttribute(proxy.getAttributePrefix(i), attrUri, proxy.getAttributeLocalName(i), proxy.getAttributeValue(i));
            } else {
                writer.writeAttribute(proxy.getAttributeLocalName(i), proxy.getAttributeValue(i));
            }
        }
    }

    @Override
    public int next() throws XMLStreamException {
        if (proxy.getEventType() == END_ELEMENT) {
            stack.removeLast();
        }
        final int eventType = proxy.next();
        if (eventType == START_ELEMENT) {
            final TagInfo parent = stack.peekLast();
            if (parent != null)
                parent.addChild(proxy.getName());
            stack.addLast(new TagInfo(proxy.getName()));
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
        return proxy.getProperty(name);
    }

    @Override
    public void require(final int type, final String namespaceURI, final String localName) throws XMLStreamException {
        proxy.require(type, namespaceURI, localName);
    }

    @Override
    public String getElementText() throws XMLStreamException {
        return proxy.getElementText();
    }

    @Override
    public boolean hasNext() throws XMLStreamException {
        return proxy.hasNext();
    }

    @Override
    public void close() throws XMLStreamException {
        proxy.close();
    }

    @Override
    public String getNamespaceURI(final String prefix) {
        return proxy.getNamespaceURI(prefix);
    }

    @Override
    public boolean isStartElement() {
        return proxy.isStartElement();
    }

    @Override
    public boolean isEndElement() {
        return proxy.isEndElement();
    }

    @Override
    public boolean isCharacters() {
        return proxy.isCharacters();
    }

    @Override
    public boolean isWhiteSpace() {
        return proxy.isWhiteSpace();
    }

    @Override
    public String getAttributeValue(final String namespaceURI, final String localName) {
        return proxy.getAttributeValue(namespaceURI, localName);
    }

    @Override
    public int getAttributeCount() {
        return proxy.getAttributeCount();
    }

    @Override
    public QName getAttributeName(final int index) {
        return proxy.getAttributeName(index);
    }

    @Override
    public String getAttributeNamespace(final int index) {
        return proxy.getAttributeNamespace(index);
    }

    @Override
    public String getAttributeLocalName(final int index) {
        return proxy.getAttributeLocalName(index);
    }

    @Override
    public String getAttributePrefix(final int index) {
        return proxy.getAttributePrefix(index);
    }

    @Override
    public String getAttributeType(final int index) {
        return proxy.getAttributeType(index);
    }

    @Override
    public String getAttributeValue(final int index) {
        return proxy.getAttributeValue(index);
    }

    @Override
    public boolean isAttributeSpecified(final int index) {
        return proxy.isAttributeSpecified(index);
    }

    @Override
    public int getNamespaceCount() {
        return proxy.getNamespaceCount();
    }

    @Override
    public String getNamespacePrefix(final int index) {
        return proxy.getNamespacePrefix(index);
    }

    @Override
    public String getNamespaceURI(final int index) {
        return proxy.getNamespaceURI(index);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return proxy.getNamespaceContext();
    }

    @Override
    public int getEventType() {
        return proxy.getEventType();
    }

    @Override
    public String getText() {
        return proxy.getText();
    }

    @Override
    public char[] getTextCharacters() {
        return proxy.getTextCharacters();
    }

    @Override
    public int getTextCharacters(final int sourceStart, final char[] target, final int targetStart, final int length) throws XMLStreamException {
        return proxy.getTextCharacters(sourceStart, target, targetStart, length);
    }

    @Override
    public int getTextStart() {
        return proxy.getTextStart();
    }

    @Override
    public int getTextLength() {
        return proxy.getTextLength();
    }

    @Override
    public String getEncoding() {
        return proxy.getEncoding();
    }

    @Override
    public boolean hasText() {
        return proxy.hasText();
    }

    @Override
    public Location getLocation() {
        return proxy.getLocation();
    }

    @Override
    public QName getName() {
        return proxy.getName();
    }

    @Override
    public String getLocalName() {
        return proxy.getLocalName();
    }

    @Override
    public boolean hasName() {
        return proxy.hasName();
    }

    @Override
    public String getNamespaceURI() {
        return proxy.getNamespaceURI();
    }

    @Override
    public String getPrefix() {
        return proxy.getPrefix();
    }

    @Override
    public String getVersion() {
        return proxy.getVersion();
    }

    @Override
    public boolean isStandalone() {
        return proxy.isStandalone();
    }

    @Override
    public boolean standaloneSet() {
        return proxy.standaloneSet();
    }

    @Override
    public String getCharacterEncodingScheme() {
        return proxy.getCharacterEncodingScheme();
    }

    @Override
    public String getPITarget() {
        return proxy.getPITarget();
    }

    @Override
    public String getPIData() {
        return proxy.getPIData();
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
            this.children = new HashMap<QName, Integer>();
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
