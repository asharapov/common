package org.echosoft.common.io.xml;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class XMLStreamReaderExtTest {

    private static XMLInputFactory inputFactory;
    private static XMLOutputFactory outputFactory;
    private XMLStreamReaderExt stream;
    private static String xml1 = "<?xml version='1.0' encoding='UTF-8'?>" +
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

    @BeforeClass
    public static void beforeClass() throws Exception {
        inputFactory = XMLInputFactory.newFactory();
        outputFactory = XMLOutputFactory.newFactory();
    }

    @Before
    public void beforeTest() throws Exception {
        stream = new XMLStreamReaderExt(inputFactory.createXMLStreamReader(new StringReader(xml1)));
    }

    @After
    public void afterTest() throws Exception {
        if (stream != null)
            stream.close();
    }

    //@Test
    public void test1() throws Exception {
        stream.nextTag();
        stream.require(XMLStreamConstants.START_ELEMENT, null, "data");
        stream.nextTag();
        stream.require(XMLStreamConstants.START_ELEMENT, null, "profiles");
        main:
        while (true) {
            int eventType = stream.next();
            switch (eventType) {
                case XMLStreamConstants.START_ELEMENT : {
                    stream.require(XMLStreamConstants.START_ELEMENT, null, "profile");
                    System.out.println("before: " + stream.getAnchorAsText() + " :  " + stream.getLocation() + "\n");
                    stream.skipTagBody();
                    System.out.println("after: " + stream.getAnchorAsText() + " :  " + stream.getLocation() + "\n");
                    System.out.println("\n-------------------------------------------\n");
                    break;
                }
                case XMLStreamConstants.END_ELEMENT : {
                    stream.require(XMLStreamConstants.END_ELEMENT, null, "profiles");
                    break main;
                }
            }
        }
        System.out.println(stream);
    }

    @Test
    public void test2() throws Exception {
        final StringWriter buf = new StringWriter(1024);
        final XMLStreamWriter writer = outputFactory.createXMLStreamWriter(buf);
        writer.writeStartDocument();
        stream.nextTag();
        stream.require(XMLStreamConstants.START_ELEMENT, null, "data");
        stream.serializeTag(writer);
        stream.require(XMLStreamConstants.END_ELEMENT, null, "data");
        writer.writeEndDocument();
        writer.close();
        Assert.assertEquals(xml1, buf.toString());
    }
}
