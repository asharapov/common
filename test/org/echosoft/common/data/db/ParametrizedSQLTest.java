package org.echosoft.common.data.db;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class ParametrizedSQLTest {

    @Test
    public void test1() throws Exception {
        final TestCase[] tests = {
                new TestCase(
                        "SELECT * FROM dual",
                        "SELECT * FROM dual"),
                new TestCase(
                        "SELECT * FROM tbl WHERE f1 = &param1 AND f2 = &param2 OR f3 = :param2",
                        "SELECT * FROM tbl WHERE f1 = ? AND f2 = ? OR f3 = ?",
                        "param1", "param2", "param2"),
                new TestCase(
                        "SELECT * FROM tbl WHERE f1 = &param1 AND f2 = &param2\r\n AND &param1>&param2",
                        "SELECT * FROM tbl WHERE f1 = ? AND f2 = ?\r\n AND ?>?",
                        "param1", "param2", "param1", "param2"),
                new TestCase(
                        "SELECT 2 & 3 as x, 1:2 as y, 't &p1 ' y, \"&p2\" x&p 3&p (&) +&",
                        "SELECT 2 & 3 as x, 1:2 as y, 't &p1 ' y, \"&p2\" x&p 3&p (&) +&"),
                new TestCase(
                        "SELECT /* &param */ FROM dual -- WHERE f1>&p\nAND f2<&p2",
                        "SELECT /* &param */ FROM dual -- WHERE f1>&p\nAND f2<?",
                        "p2"),
        };
        for (TestCase test : tests) {
            final ParameterizedSQL psql = new ParameterizedSQL(test.namedSQL);
            Assert.assertEquals(test.namedSQL + "  ", test.unnamedSQL, psql.getQuery());
            Assert.assertEquals(test.namedSQL + "  ", test.params, psql.getParamNames());
        }
    }


    private static class TestCase {
        private final String namedSQL;
        private final String unnamedSQL;
        private final List<String> params;

        private TestCase(final String namedSQL, final String unnamedSQL, final String... params) {
            this.namedSQL = namedSQL;
            this.unnamedSQL = unnamedSQL;
            this.params = Collections.unmodifiableList(Arrays.asList(params));

        }
    }
}
