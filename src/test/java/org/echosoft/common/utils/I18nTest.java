package org.echosoft.common.utils;

import java.util.Locale;
import java.util.MissingResourceException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class I18nTest {

    private static final Locale locale = new Locale("ru", "RU");
    @BeforeClass
    public static void beforeClass() {
        I18n.registerBundle("test", I18nTest.class.getPackage().getName() + ".messages");
    }

    @Test
    public void testGetMessage() {
        for (int i=1; i<=1000; i++) {
            final String result = I18n.getMessage("test@key."+i, locale);
            Assert.assertEquals("значение."+i, result);
        }
        final String pval1 = I18n.getMessage("test@pkey.1", new Object[]{1,2}, locale);
        Assert.assertEquals("значение 1 / 2", pval1);
        final String pval2 = I18n.getMessage("test@pkey.1", new Object[]{'A','B'}, locale);
        Assert.assertEquals("значение A / B", pval2);
        try {
            I18n.getMessage("test@xxx", Locale.ENGLISH);
            Assert.fail("should be raised MissingResourceException");
        } catch (MissingResourceException e) {
        }
    }

    @Test
    public void testStaticMessage() {
        final I18n.Message m1 = I18n.makeStaticMessage("static message");
        final I18n.Message m2 = I18n.makeStaticMessage("static message");
        Assert.assertNotNull(m1);
        Assert.assertEquals(m1, m2);
        Assert.assertEquals("static message", m1.getString(locale));
        Assert.assertEquals("static message", m1.getString(Locale.ENGLISH));
        Assert.assertEquals("static message", m1.getString());
    }

    @Test
    public void testDynaMessage() {
        final I18n.Message m1 = I18n.makeMessage("test@key.1");
        final I18n.Message m2 = I18n.makeMessage("test@key.1");
        final I18n.Message m3 = I18n.makeMessage("test@pkey.1", "A", "B");
        Assert.assertNotNull(m1);
        Assert.assertNotNull(m2);
        Assert.assertNotNull(m3);
        Assert.assertEquals(m1, m2);
        Assert.assertEquals("значение.1", m1.getString(locale));
        Assert.assertEquals("value.1", m1.getString(Locale.ENGLISH));
        Assert.assertEquals("значение A / B", m3.getString(locale));
        Assert.assertEquals("value A / B", m3.getString(Locale.ENGLISH));
    }

}
