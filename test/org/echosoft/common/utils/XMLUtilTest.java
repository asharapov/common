package org.echosoft.common.utils;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Anton Sharapov
 */
public class XMLUtilTest {

    private Document doc;

    @Before
    public void beforeTest() throws Exception {
        doc = XMLUtil.createDocument();
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
    }

    @Test
    public void testXPath() throws Exception {
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
}
