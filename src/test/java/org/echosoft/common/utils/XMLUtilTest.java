package org.echosoft.common.utils;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.net.URL;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Anton Sharapov
 */
public class XMLUtilTest {

    private static final String NS_TEST1 = "http://schemas.echo.org/test/1";

    private static URL res1;
    private static URL res2;

    @BeforeClass
    public static void beforeClass() throws Exception {
        res1 = XMLUtilTest.class.getResource("example.xml");
        res2 = XMLUtilTest.class.getResource("example-ns.xml");
    }


    @Test
    public void test1() throws Exception {
        final Document doc = XMLUtil.loadDocument(res1.openStream());
        final Element root = doc.getDocumentElement();
        final Element cc = XMLUtil.getChildElement(root, "customers");
        Assert.assertNotNull(cc);
        Assert.assertNull(cc.getNamespaceURI());
        Assert.assertNull(cc.getLocalName());
        Assert.assertEquals("customers", cc.getTagName());
        Assert.assertEquals("customers", cc.getNodeName());
        for (Element el : XMLUtil.getChildElements(cc)) {
            Assert.assertNotNull(el);
            Assert.assertNull(el.getNamespaceURI());
            Assert.assertNull(el.getLocalName());
            Assert.assertEquals("customer", el.getTagName());
            Assert.assertEquals("customer", el.getNodeName());
        }
    }

    @Test
    public void test2() throws Exception {
        final Document doc = XMLUtil.loadDocument(res2.openStream());
        final Element root = doc.getDocumentElement();
        final Element cc = XMLUtil.getChildElement(root, "tst:customers");
        Assert.assertNotNull(cc);
        Assert.assertNull(cc.getNamespaceURI());
        Assert.assertNull(cc.getLocalName());
        Assert.assertEquals("tst:customers", cc.getTagName());
        Assert.assertEquals("tst:customers", cc.getNodeName());
        for (Element el : XMLUtil.getChildElements(cc)) {
            Assert.assertNotNull(el);
            Assert.assertNull(el.getNamespaceURI());
            Assert.assertNull(el.getLocalName());
            Assert.assertEquals("tst:customer", el.getTagName());
            Assert.assertEquals("tst:customer", el.getNodeName());
        }
    }

    @Test
    public void test3() throws Exception {
        final Document doc = XMLUtil.loadDocument(res2.openStream(), true, null);
        final Element root = doc.getDocumentElement();
        final Element cc = XMLUtil.getChildElement(root, NS_TEST1, "customers");
        Assert.assertNotNull(cc);
        Assert.assertEquals(NS_TEST1, cc.getNamespaceURI());
        Assert.assertEquals("customers", cc.getLocalName());
        Assert.assertEquals("tst:customers", cc.getTagName());
        Assert.assertEquals("tst:customers", cc.getNodeName());
        for (Element el : XMLUtil.getChildElements(cc)) {
            Assert.assertNotNull(el);
            Assert.assertEquals(NS_TEST1, el.getNamespaceURI());
            Assert.assertEquals("customer", el.getLocalName());
            Assert.assertEquals("tst:customer", el.getTagName());
            Assert.assertEquals("tst:customer", el.getNodeName());
        }
        System.out.println(doc);
    }

    @Test
    public void testXPath() throws Exception {
        final Document doc = makeDocument();
        System.out.println( XMLUtil.serialize(doc.getDocumentElement()) );
        System.out.println();
        System.out.println();
        final XPath xpath = XPathFactory.newInstance().newXPath();
        System.out.println("1. query for list ...");
        NodeList nlist = (NodeList)xpath.evaluate("//contract", doc, XPathConstants.NODESET);
        for (int i=0; i<nlist.getLength(); i++) {
            System.out.println( nlist.item(i) );
        }
        System.out.println("2. query for node ...");
        Node node = (Node)xpath.evaluate("//contract[@number='X1']/item[@number=1]", doc, XPathConstants.NODE);
        System.out.println(node);
        String text = XMLUtil.queryText(doc, "//contract[@number='X1']/@date");
        System.out.println(text);
    }

    private static Document makeDocument() throws Exception {
        final Document doc = XMLUtil.createDocument();
        final Element root = (Element)doc.appendChild( doc.createElement("root") );
        for (int c=1; c<3; c++) {
            final Element contract = (Element)root.appendChild( doc.createElement("contract") );
            contract.setAttribute("number", "X"+c);
            contract.setAttribute("date", "01.0"+c+".2008");
            for (int i=1; i<=10; i++) {
                final Element item = (Element)contract.appendChild( doc.createElement("item") );
                item.setAttribute("number", Integer.toString(i));
                item.setAttribute("title", "Item №"+i);
                final Element contragent = (Element)item.appendChild( doc.createElement("contragent"));
                contragent.setAttribute("name", "contragent for "+i+" item");
            }
        }
        return doc;
    }

}
