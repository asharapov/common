package org.echosoft.common.io.xml;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamWriter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class XMLStreamReaderExtTest {

    private static XMLInputFactory inputFactory;
    private static XMLOutputFactory outputFactory;
    private XMLStreamReaderExt xmlr1, xmlr2;
    private static String test1 = "<?xml version='1.0' encoding='UTF-8'?>" +
            "<data xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:ts=\"http://ts.ru/schema\">\n" +
            " <profiles>\n" +
            "  <profile id=\"1\" ts:item=\"&quot;test&quot;\">\n" +
            "    <?compiler attr1=\"1\" attr2=\"2\"?>\n" +
            "    <last-name>Ivanov</last-name>\n" +
            "    <first-name>Ivan</first-name>\n" +
            "    <age>25</age>\n" +
            "    <tags>\n" +
            "      <tag>A1</tag>\n" +
            "      <tag>B1</tag>\n" +
            "    </tags>\n" +
            "  </profile>\n" +
            "  <profile id=\"2\">\n" +
            "    <![CDATA[ hello world ]]>\n" +
            "    <last-name>Petrov</last-name>\n" +
            "    <first-name>Petr</first-name>\n" +
            "    <age>28</age>\n" +
            "    <tags>\n" +
            "      <tag>A2</tag>\n" +
            "      <tag>B2</tag>\n" +
            "    </tags>\n" +
            "  </profile>\n" +
            "  <profile id=\"3\">\n" +
            "   <last-name>Svetova</last-name>\n" +
            "   <first-name>Sveta</first-name>\n" +
            "   <age>18</age>\n" +
            "    <tags>\n" +
            "      <tag>A3</tag>\n" +
            "      <tag>B3</tag>\n" +
            "    </tags>\n" +
            "  </profile>\n" +
            " </profiles>\n" +
            "</data>";

    private static String test2 = "<?xml version='1.0' encoding='UTF-8'?>" +
            //"<!DOCTYPE rdf:RDF SYSTEM \"http://dublincore.org/2000/12/01-dcmes-xml-dtd.dtd\">\n" +
            "<rdf:RDF xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" rdf:about=\"http://dublincore.org/\">\n" +
            "<!-- Sample from http://dublincore.org/documents/2002/04/22/dcmes-xml/-->\n" +
            "    <rdf:Description rdf:about=\"http://dublincore.org/\">\n" +
            "        <dc:title>Dublin Core Metadata Initiative - Home Page</dc:title>\n" +
            "        <dc:description>The Dublin Core Metadata Initiative Web site.</dc:description>\n" +
            "        <dc:date>1998-10-10</dc:date>\n" +
            "        <dc:format>text/html</dc:format>\n" +
            "        <dc:language>en</dc:language>\n" +
            "        <dc:contributor>The Dublin Core Metadata Initiative</dc:contributor>\n" +
            "        <!-- My guess at the French for the above Dave -->\n" +
            "        <dc:contributor xml:lang=\"fr\">L'Initiative de métadonnées du Dublin Core</dc:contributor>\n" +
            "        <dc:contributor xml:lang=\"de\">der Dublin-Core Metadata-Diskussionen</dc:contributor>\n" +
            "    </rdf:Description>\n" +
            "</rdf:RDF>";

    @BeforeClass
    public static void beforeClass() throws Exception {
        inputFactory = XMLInputFactory.newFactory();
        outputFactory = XMLOutputFactory.newFactory();
    }

    @Before
    public void beforeTest() throws Exception {
        xmlr1 = new XMLStreamReaderExt(inputFactory.createXMLStreamReader(new StringReader(test1)));
        xmlr2 = new XMLStreamReaderExt(inputFactory.createXMLStreamReader(new StringReader(test2)));
    }

    @After
    public void afterTest() throws Exception {
        if (xmlr1 != null)
            xmlr1.close();
        if (xmlr2 != null)
            xmlr2.close();
    }

    @Test
    public void test1() throws Exception {
        xmlr1.nextTag();
        xmlr1.require(XMLStreamConstants.START_ELEMENT, null, "data");
        xmlr1.nextTag();
        xmlr1.require(XMLStreamConstants.START_ELEMENT, null, "profiles");
        main:
        while (true) {
            int eventType = xmlr1.next();
            switch (eventType) {
                case XMLStreamConstants.START_ELEMENT : {
                    xmlr1.require(XMLStreamConstants.START_ELEMENT, null, "profile");
                    System.out.println(xmlr1.getAnchorAsText());
                    final Element elem = xmlr1.getElement();
                    if ("2".equals(elem.getAttribute("id"))) {
                        final StringBuilder buf = new StringBuilder();
                        elem.writeOpenTag(buf);
                        elem.writeCloseTag(buf);
                        System.out.println(buf);
                        xmlr1.skipTagBody();
                    } else {
                        xmlr1.skipTagBody();
                    }
                    break;
                }
                case XMLStreamConstants.END_ELEMENT : {
                    xmlr1.require(XMLStreamConstants.END_ELEMENT, null, "profiles");
                    break main;
                }
            }
        }
        System.out.println(xmlr1);
    }

    @Test
    public void test2() throws Exception {
        final StringWriter buf = new StringWriter(1024);
        final XMLStreamWriter xmlw = outputFactory.createXMLStreamWriter(buf);
        xmlw.writeStartDocument();
        xmlr1.nextTag();
        xmlr1.require(XMLStreamConstants.START_ELEMENT, null, "data");
        xmlr1.serializeTag(xmlw);
        xmlr1.require(XMLStreamConstants.END_ELEMENT, null, "data");
        xmlw.writeEndDocument();
        xmlw.close();
        Assert.assertEquals(test1, buf.toString());
    }

    @Test
    public void test3() throws Exception {
        final StringWriter buf = new StringWriter(1024);
        final XMLStreamWriter xmlw = outputFactory.createXMLStreamWriter(buf);
        xmlw.writeStartDocument();
        xmlr1.nextTag();
        xmlr1.require(XMLStreamConstants.START_ELEMENT, null, "data");
        xmlr1.serializeTag(xmlw);
        xmlw.writeEndDocument();
        xmlw.close();
        Assert.assertEquals(test1, buf.toString());
    }

    @Test
    public void test4() throws Exception {
        final StringWriter buf = new StringWriter(1024);
        final XMLStreamWriter xmlw = outputFactory.createXMLStreamWriter(buf);
        xmlw.writeStartDocument();
        xmlr2.nextTag();
        xmlr2.require(XMLStreamConstants.START_ELEMENT, "http://www.w3.org/1999/02/22-rdf-syntax-ns#", "RDF");
        final Element root = xmlr2.getElement();
        final StringBuilder buf2 = new StringBuilder();
        root.writeOpenTag(buf2);
        root.writeCloseTag(buf2);
        System.out.println(buf2);
        xmlr2.serializeTag(xmlw);
        xmlw.writeEndDocument();
        xmlw.close();
        Assert.assertEquals(test2, buf.toString());
    }
}
