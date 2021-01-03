package org.echosoft.common.io.xml;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.net.URL;

import com.sun.org.apache.xerces.internal.impl.Constants;
import org.echosoft.common.utils.StreamUtil;
import org.echosoft.common.utils.XMLUtilTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class XMLStreamReaderExtTest {

    private static final String NS_TEST1 = "http://schemas.echo.org/test/1";

    private static URL res1;
    private static URL res2;
    private static XMLInputFactory inputFactory;
    private static XMLOutputFactory outputFactory;

    private XMLStreamReaderExt xmlr1, xmlr2;

    @BeforeClass
    public static void beforeClass() throws Exception {
        inputFactory = XMLInputFactory.newFactory();
        inputFactory.setProperty(Constants.ZEPHYR_PROPERTY_PREFIX + Constants.STAX_REPORT_CDATA_EVENT, true);   // если требуется обработка CDATA токенов
        outputFactory = XMLOutputFactory.newFactory();
        res1 = XMLUtilTest.class.getResource("example.xml");
        res2 = XMLUtilTest.class.getResource("example-ns.xml");
    }

    @Before
    public void beforeTest() throws Exception {
        xmlr1 = new XMLStreamReaderExt(inputFactory.createXMLStreamReader(res1.openStream()));
        xmlr2 = new XMLStreamReaderExt(inputFactory.createXMLStreamReader(res2.openStream()));
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
        xmlr1.require(XMLStreamConstants.START_ELEMENT, null, "customers");
        main:
        while (true) {
            int eventType = xmlr1.next();
            switch (eventType) {
                case XMLStreamConstants.START_ELEMENT : {
                    xmlr1.require(XMLStreamConstants.START_ELEMENT, null, "customer");
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
                    xmlr1.require(XMLStreamConstants.END_ELEMENT, null, "customers");
                    break main;
                }
            }
        }
        System.out.println(xmlr1);
    }

    @Test
    public void test2() throws Exception {
        final String original = new String(StreamUtil.streamToBytes(res1.openStream()), "UTF-8");

        final StringWriter buf = new StringWriter(1024);
        final XMLStreamWriter xmlw = outputFactory.createXMLStreamWriter(buf);
        xmlw.writeStartDocument("UTF-8", "1.0");
        xmlw.writeCharacters("\n");
        xmlr1.nextTag();
        xmlr1.require(XMLStreamConstants.START_ELEMENT, null, "data");
        xmlr1.serializeTag(xmlw);
        xmlr1.require(XMLStreamConstants.END_ELEMENT, null, "data");
        xmlw.writeEndDocument();
        xmlw.close();

        Assert.assertEquals(original, buf.toString());
    }

    @Test
    public void test3() throws Exception {
        final String original = new String(StreamUtil.streamToBytes(res1.openStream()), "UTF-8");

        final StringWriter buf = new StringWriter(1024);
        final XMLStreamWriter xmlw = outputFactory.createXMLStreamWriter(buf);
        xmlw.writeStartDocument("UTF-8", "1.0");
        xmlw.writeCharacters("\n");
        xmlr1.nextTag();
        xmlr1.require(XMLStreamConstants.START_ELEMENT, null, "data");
        xmlr1.serializeTag(xmlw);
        xmlw.writeEndDocument();
        xmlw.close();

        Assert.assertEquals(original, buf.toString());
    }

    @Test
    public void test4() throws Exception {
        final String original = new String(StreamUtil.streamToBytes(res2.openStream()), "UTF-8");

        final StringWriter buf = new StringWriter(1024);
        final XMLStreamWriter xmlw = outputFactory.createXMLStreamWriter(buf);
        xmlw.writeStartDocument("UTF-8", "1.0");
        xmlw.writeCharacters("\n");
        xmlr2.nextTag();
        xmlr2.require(XMLStreamConstants.START_ELEMENT, NS_TEST1, "data");
        final Element root = xmlr2.getElement();
        final StringBuilder buf2 = new StringBuilder();
        root.writeOpenTag(buf2);
        root.writeCloseTag(buf2);
        System.out.println(buf2);
        xmlr2.serializeTag(xmlw);
        xmlw.writeEndDocument();
        xmlw.close();

        Assert.assertEquals(original, buf.toString());
    }
}
