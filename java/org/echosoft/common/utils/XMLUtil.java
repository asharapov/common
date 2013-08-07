package org.echosoft.common.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.echosoft.common.io.FastStringWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Содержит методы, часто используемые при работе с DOM моделью документов XML.
 *
 * @author Andrey Ochirov
 * @author Anton Sharapov
 */
public class XMLUtil {

    private static final Properties SER_PROPS;
    static {
        SER_PROPS = new Properties();
        SER_PROPS.put(OutputKeys.OMIT_XML_DECLARATION, "no");
        SER_PROPS.put(OutputKeys.INDENT, "yes");
        SER_PROPS.put(OutputKeys.METHOD, "xml");
        SER_PROPS.put("{http://xml.apache.org/xslt}indent-amount", "2");
    }

    /**
     * Constructor of <code>XMLUtil</code> declared as private
     * to prevent its direct instantiation.
     */
    private XMLUtil() {
    }


    /**
     * Loads document from file, that name specified as parameter to this method.
     *
     * @param file the file name, that contains XML document.
     * @return the <code>Document</code> interface instance that represents the entire XML document.
     * @throws IOException                  in case of any io errors.
     * @throws ParserConfigurationException in case of any parsing errors.
     * @throws SAXException                 in case of any xml parsing errors.
     */
    public static Document loadDocument(final File file) throws IOException, ParserConfigurationException, SAXException {
        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final FileInputStream in = new FileInputStream(file);
        try {
            return builder.parse(new InputSource(in));
        } finally {
            in.close();
        }
    }

    /**
     * Loads document from the URL, that specified as parameter to this method. Also, the URL can referencing to the file.
     *
     * @param url the URL to the XML source.
     * @return the <code>Document</code> interface instance that represents the entire XML document.
     * @throws IOException                  in case of any io errors.
     * @throws ParserConfigurationException in case of any parsing errors.
     * @throws SAXException                 in case of any xml parsing errors.
     */
    public static Document loadDocument(final URL url) throws IOException, ParserConfigurationException, SAXException {
        if (url.getProtocol().equalsIgnoreCase("file")) {
            final String fileName = StringUtil.trim(url.getHost()) != null ? StringUtil.trim(url.getHost()) : url.getFile();
            return loadDocument(new File(fileName));
        } else {
            final InputStream in = url.openStream();
            try {
                return loadDocument(in);
            } finally {
                in.close();
            }
        }
    }

    /**
     * Loads document from the input stream, that specified as parameter to this method.
     *
     * @param in the input stream of XML document.
     * @return the <code>Document</code> interface instance that represents the entire XML document.
     * @throws IOException                  in case of any io errors.
     * @throws ParserConfigurationException in case of any parsing errors.
     * @throws SAXException                 in case of any xml parsing errors.
     */
    public static Document loadDocument(final InputStream in) throws IOException, ParserConfigurationException, SAXException {
        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return builder.parse(new InputSource(in));
    }


    /**
     * Gets <code>TEXT</code> element for node.
     *
     * @param node node with text.
     * @return text, that contains the specified node.
     */
    public static String getNodeText(final Node node) {
//        node.normalize();
        StringBuilder buf = null;
        final NodeList list = node.getChildNodes();
        for (int i = 0, len = list.getLength(); i < len; i++) {
            final Node n = list.item(i);
            final int type = n.getNodeType();
            if (type == Node.TEXT_NODE || type == Node.CDATA_SECTION_NODE) {
                final String value = n.getNodeValue();
                if (value != null && value.length() > 0) {
                    if (buf == null)
                        buf = new StringBuilder(value.length());
                    buf.append(value);
                }
            }
        }
        return buf != null ? StringUtil.trim(buf.toString()) : null;
    }


    /**
     * Extracts child elements from node whith type <code>ELEMENT_NODE</code>.
     *
     * @param node root node of XML document for search.
     * @return iterator with proper node childs.
     */
    public static Iterator<Element> getChildElements(final Node node) {
//        node.normalize();
        return new Iterator<Element>() {
            private final NodeList nodes = node.getChildNodes();
            private int nextPos = 0;
            private Element nextElement = seekNext();

            public boolean hasNext() {
                return nextElement != null;
            }
            public Element next() {
                if (nextElement == null)
                    throw new NoSuchElementException();
                final Element result = nextElement;
                nextElement = seekNext();
                return result;
            }
            public void remove() {
                throw new UnsupportedOperationException("operation not supported");
            }
            private Element seekNext() {
                for (int i = nextPos, len = nodes.getLength(); i < len; i++) {
                    final Node childNode = nodes.item(i);
                    if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                        nextPos = i + 1;
                        return (Element) childNode;
                    }
                }
                return null;
            }
        };
    }


    /**
     * Extracts child elements with given name from node whith type <code>ELEMENT_NODE</code>.
     *
     * @param node        root node of XML document for search.
     * @param elementName name of elements that should be returned.
     * @return iterator with proper node childs.
     */
    public static Iterator<Element> getChildElements(final Node node, final String elementName) {
//        node.normalize();
        return new Iterator<Element>() {
            private final NodeList nodes = node.getChildNodes();
            private int nextPos = 0;
            private Element nextElement = seekNext();

            public boolean hasNext() {
                return nextElement != null;
            }
            public Element next() {
                if (nextElement == null)
                    throw new NoSuchElementException();
                final Element result = nextElement;
                nextElement = seekNext();
                return result;
            }
            public void remove() {
                throw new UnsupportedOperationException("operation not supported");
            }
            private Element seekNext() {
                for (int i = nextPos, len = nodes.getLength(); i < len; i++) {
                    final Node childNode = nodes.item(i);
                    if (childNode.getNodeType() == Node.ELEMENT_NODE && childNode.getNodeName().equals(elementName)) {
                        nextPos = i + 1;
                        return (Element) childNode;
                    }
                }
                return null;
            }
        };
    }

    /**
     * Gets first child element with specified name.
     *
     * @param parentNode parent node.
     * @param childName  child name.
     * @return first childr element with specified name.
     */
    public static Element getChildElement(final Node parentNode, final String childName) {
//        parentNode.normalize();
        final NodeList nodeList = parentNode.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(childName))
                return (Element) node;
        }
        return null;
    }


    public static Element getNextSiblingElement(Node node, final String siblingName) {
        do {
            node = node.getNextSibling();
            if (node == null)
                return null;
        } while (node.getNodeType() != Node.ELEMENT_NODE || !node.getNodeName().equals(siblingName));
        return (Element) node;
    }


    /**
     * Gets <code>TEXT</code> parentElement for child parentElement
     *
     * @param parentElement parent parentElement.
     * @param childName     child parentElement name.
     * @return text, that contains the specified child parentElement.
     */
    public static String getChildNodeText(final Element parentElement, final String childName) {
        final Element childElement = getChildElement(parentElement, childName);
        return childElement != null ? getNodeText(childElement) : null;
    }


    /**
     * Gets <code>CDATASection</code> element for node.
     *
     * @param node node with text.
     * @return text, that contains the specified node.
     */
    public static String getNodeCDATASection(final Node node) {
//        node.normalize();
        final NodeList list = node.getChildNodes();
        for (int i = 0, len = list.getLength(); i < len; i++) {
            final Node child = list.item(i);
            if (child.getNodeType() == Node.CDATA_SECTION_NODE)
                return child.getNodeValue();
        }
        return null;
    }


    /**
     * Select node list what matches given xpath query
     *
     * @param doc        xml document
     * @param expression xpath query
     * @return nodes which confirms given xpath query.
     * @throws XPathExpressionException in case of any errors.
     */
    public static NodeList query(final Document doc, final String expression) throws XPathExpressionException {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        return (NodeList) xpath.evaluate(expression, doc.getDocumentElement(), XPathConstants.NODESET);
    }

    /**
     * Select node list what matches given xpath query
     *
     * @param node       xml node
     * @param expression xpath query
     * @return nodes which confirms given xpath query.
     * @throws XPathExpressionException in case of any errors.
     */
    public static NodeList query(final Node node, final String expression) throws XPathExpressionException {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        return (NodeList) xpath.evaluate(expression, node, XPathConstants.NODESET);
    }


    /**
     * Select only one node what matches given xpath query
     *
     * @param doc        xml document
     * @param expression xpath query
     * @return first element which confirms given xpath query.
     * @throws XPathExpressionException in case of any errors.
     */
    public static Element queryElement(final Document doc, final String expression) throws XPathExpressionException {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        return (Element) xpath.evaluate(expression, doc, XPathConstants.NODE);
    }


    /**
     * Select only one node what matches given xpath query
     *
     * @param node       xml node
     * @param expression xpath query
     * @return first element which confirms given xpath query.
     * @throws XPathExpressionException in case of any errors.
     */
    public static Element queryElement(final Node node, final String expression) throws XPathExpressionException {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        return (Element) xpath.evaluate(expression, node, XPathConstants.NODE);
    }


    /**
     * Select only one node what matches given xpath query
     *
     * @param doc   xml document
     * @param xpath xpath query
     * @return text under element which confirms given xpath query
     * @throws XPathExpressionException in case of any errors.
     */
    public static String queryText(final Document doc, final String xpath) throws XPathExpressionException {
        return queryText(doc.getDocumentElement(), xpath);
    }


    /**
     * Select only one node what matches given xpath query
     *
     * @param node       xml node
     * @param expression xpath query
     * @return text under element which confirms given xpath query
     * @throws XPathExpressionException in case of any errors.
     */
    public static String queryText(final Node node, final String expression) throws XPathExpressionException {
        final XPath xpath = XPathFactory.newInstance().newXPath();
        final Node n = (Node) xpath.evaluate(expression, node, XPathConstants.NODE);
        if (n == null)
            return null;
        return (n.getNodeType() == Node.TEXT_NODE) ? n.getNodeValue() : getNodeText(n);
    }


    /**
     * Create new DOM document.
     *
     * @return new xml DOM document.
     * @throws ParserConfigurationException in case of any errors.
     */
    public static Document createDocument() throws ParserConfigurationException {
        final DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
        dFactory.setNamespaceAware(true);
        final DocumentBuilder docBuilder = dFactory.newDocumentBuilder();
        return docBuilder.newDocument();
    }


    /**
     * Create new DOM document from XMl string.
     *
     * @param xml text of the serialized XML document.
     * @return new XML DOC document.
     * @throws ParserConfigurationException in case of parsing errors.
     * @throws IOException                  in case of io errors.
     * @throws SAXException                 in case errors.
     */
    public static Document createDocument(final String xml) throws ParserConfigurationException, IOException, SAXException {
        final DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
        dFactory.setNamespaceAware(true);
        final DocumentBuilder docBuilder = dFactory.newDocumentBuilder();
        final InputSource xmlSourse = new InputSource(new StringReader(xml));
        return docBuilder.parse(xmlSourse);
    }


    /**
     * Serialize document to string.
     *
     * @param node node which must be serialized.
     * @return serialized form of the node.
     * @throws TransformerException in case of any errors.
     */
    public static String serialize(final Node node) throws TransformerException {
        final Transformer serializer = TransformerFactory.newInstance().newTransformer();
        serializer.setOutputProperties(SER_PROPS);
        final FastStringWriter writer = new FastStringWriter();
        serializer.transform(new DOMSource(node), new StreamResult(writer));
        return writer.toString();
    }


    /**
     * Serialize document to the output file.
     *
     * @param node       node which must be serialized.
     * @param outputFile output file.
     * @throws TransformerException in case of any errors.
     * @throws IOException          in case any io errors.
     */
    public static void serialize(final Node node, final File outputFile) throws TransformerException, IOException {
        final Transformer serializer = TransformerFactory.newInstance().newTransformer();
        serializer.setOutputProperties(SER_PROPS);
        final FileOutputStream out = new FileOutputStream(outputFile);
        try {
            serializer.transform(new DOMSource(node), new StreamResult(out));
        } finally {
            out.close();
        }
    }


    /**
     * Process the source DOM tree to the output DOM tree
     *
     * @param xmlDoc model DOM tree
     * @param xslDoc template DOM tree
     * @return produced DOM tree.
     * @throws TransformerException         in case of any errors due documents transformation
     * @throws ParserConfigurationException in case of any errors due documents parsing
     */
    public static Document apply(final Document xmlDoc, final Document xslDoc) throws TransformerException, ParserConfigurationException {
        final Transformer transformer = TransformerFactory.newInstance().newTransformer(new DOMSource(xslDoc));
        final Document targetDoc = XMLUtil.createDocument();
        transformer.transform(new DOMSource(xmlDoc), new DOMResult(targetDoc));
        return targetDoc;
    }


    /**
     * Process the source document to the output document
     *
     * @param xmlFile  file with model DOM tree
     * @param xslFile  file with template DOM tree
     * @param htmlFile file with produced DOM tree.
     * @throws TransformerException in case of any errors due documents transformation
     */
    public static void apply(final File xmlFile, final File xslFile, final File htmlFile) throws TransformerException {
        final Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(xslFile));
        transformer.transform(new StreamSource(xmlFile), new StreamResult(htmlFile));
    }
}
