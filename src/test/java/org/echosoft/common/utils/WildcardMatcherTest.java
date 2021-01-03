package org.echosoft.common.utils;

import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class WildcardMatcherTest {

    @Test
    public void test() {
        final String[][] tests = new String[][]{
                {"", "", " ", "abc", "1xxx2"},
                {"abc*", "", "ab", "abc", "abcd", "abcdef"},
                {"*def", "", "abc", "abcd", "abcdef", "def", "define"},
                {"abc*def", "abc", "def", "abcdef", "abcxxxdef", "abcdefabcdef"}
        };
        for (String[] test : tests) {
            final String pattern = test[0];
            final WildcardMatcher matcher = WildcardMatcher.makeMatcher(pattern, "*xxx*");
            for (int i=1; i<test.length; i++) {
                final String text = test[i];
                System.out.print(matcher+":    text:'"+text+"'   =   ");
                System.out.println(matcher.match(text));
            }
            System.out.println();
        }
    }
}
