package org.echosoft.common.utils;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class MimeUtilTest {

    @Test
    public void test1() throws Exception {
        String mime = MimeUtil.getMimeTypeForExtension("html", null);
        Assert.assertNotNull(mime);
        Collection<String> exts = MimeUtil.getExtensionsForMimeType(mime);
        Assert.assertTrue(exts != null && exts.size() >= 0);
        Assert.assertTrue(exts.contains("html"));
    }
}
