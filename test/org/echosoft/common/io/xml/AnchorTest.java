package org.echosoft.common.io.xml;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class AnchorTest {

    @Test
    public void testValidAnchors() throws Exception {
        final String[][] tests = {
                {null, "/"},
                {"", "/"},
                {"/", "/"},
                {"/a/b/c", "/a[1]/b[1]/c[1]"},
                {"/a[1]/b[2]/c[3]", "/a[1]/b[2]/c[3]"},
                {"/ws:config/ws:profiles/ws:profile[3]/ws:last-name", "/ws:config[1]/ws:profiles[1]/ws:profile[3]/ws:last-name[1]"},
                {" ws : config /ws:profiles/ ws : profile [ 3 ] /ws:last-name/", "/ws:config[1]/ws:profiles[1]/ws:profile[3]/ws:last-name[1]"}
        };
        for (String[] test : tests) {
            final Anchor anchor = Anchor.parseString(test[0]);
            Assert.assertEquals(test[1], anchor.toString());
        }
    }

    @Test
    public void testInvalidAnchors() throws Exception {
        final String[] tests = {
                "/a/b/c[",
                "/a/b/c[1",
                "/a/b/c[]"
        };
        for (String test : tests) {
            try {
                final Anchor anchor = Anchor.parseString(test);
                Assert.fail(test + "  :  " + anchor);
            } catch (Exception e) {
            }
        }
    }
}
