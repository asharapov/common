package org.echosoft.common.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Класс предназначен для проверки строковых выражений на соответствие с определенным шаблоном.
 * В шаблоне можно указывать специальные символы '?' и '*' которые определяют один или произвольное количество любых символов.<br/>
 * <pre>
 * пр:
 * makeMatcher("*.txt").match("c.txt")      --> true
 * makeMatcher("*.jpg").match("c.txt")      --> false
 * makeMatcher("a/b/*").match("a/b/c.txt")  --> true
 * makeMatcher("*.???").match("c.txt")      --> true
 * makeMatcher("*.????").match("c.txt")     --> false
 * </pre>
 *
 * Алгоритм взят из проекта Apache Commons-IO.
 * @author Anton Sharapov
 */
public class WildcardMatcher implements Serializable {

    public static WildcardMatcher makeMatcher(final String template, final String... altTemplates) {
        return makeMatcher(true, template, altTemplates);
    }

    public static WildcardMatcher makeMatcher(final boolean caseSensitive, final String template, final String... altTemplates) {
        final String[] templates = new String[1+altTemplates.length];
        templates[0] = template;
        System.arraycopy(altTemplates, 0, templates, 1, altTemplates.length);
        return new WildcardMatcher(caseSensitive, templates);
    }

    private final boolean caseSensitive;
    private final String[] rawPatterns;
    private final String[][] compiledPatterns;


    public WildcardMatcher(final boolean caseSensitive, final String[] templates) {
        if (templates==null || templates.length==0)
            throw new IllegalArgumentException("Templates must be specified");

        this.caseSensitive = caseSensitive;
        this.rawPatterns = templates;
        final ArrayList<String[]> patterns = new ArrayList<String[]>(templates.length);

        final ArrayList<String> pchunks = new ArrayList<String>(5);
        final StringBuilder buf = new StringBuilder(32);

        for (String template : templates) {
            if (template==null)
                throw new IllegalArgumentException("Template can't be null");
            if (!caseSensitive)
                template = template.toUpperCase();

            if (template.indexOf("?") == -1 && template.indexOf("*") == -1) {
                patterns.add( new String[]{template} );
            } else {
                pchunks.clear();
                buf.setLength(0);
                for (char c : template.toCharArray()) {
                    if (c == '?' || c == '*') {
                        if (buf.length() != 0) {
                            pchunks.add( buf.toString() );
                            buf.setLength(0);
                        }
                        if (c == '?') {
                            pchunks.add("?");
                        } else
                        if ( pchunks.size()==0 || !"*".equals(pchunks.get(pchunks.size()-1)) ) {
                            pchunks.add("*");
                        }
                    } else {
                        buf.append(c);
                    }
                }
                if (buf.length() != 0) {
                    pchunks.add(buf.toString());
                }
                patterns.add( pchunks.toArray(new String[pchunks.size()]) );
            }
        }
        this.compiledPatterns = patterns.toArray( new String[patterns.size()][] );
    }

    public boolean match(String text) {
        if (text==null)
            return false;
        if (!caseSensitive)
            text = text.toUpperCase();

        for (final String[] pattern : compiledPatterns) {
            if (match0(text, pattern))
                return true;
        }
        return false;
    }

    private static boolean match0(final String text, final String[] wcs) {
        boolean anyChars = false;
        int textIdx = 0;
        int wcsIdx = 0;
        final LinkedList<int[]> backtrack = new LinkedList<int[]>();

        // loop around a backtrack stack, to handle complex * matching
        do {
            if (backtrack.size() > 0) {
                final int[] array = backtrack.pop();
                wcsIdx = array[0];
                textIdx = array[1];
                anyChars = true;
            }

            // loop whilst tokens and text left to process
            while (wcsIdx < wcs.length) {
                final String pattern = wcs[wcsIdx];

                if (pattern.equals("?")) {
                    textIdx++;
                    anyChars = false;

                } else
                if (pattern.equals("*")) {
                    anyChars = true;
                    if (wcsIdx == wcs.length - 1) {
                        textIdx = text.length();
                    }
                } else {
                    // matching text token
                    if (anyChars) {
                        // any chars then try to locate text token
                        textIdx = text.indexOf(pattern, textIdx);
                        if (textIdx == -1) {
                            // token not found
                            break;
                        }
                        final int repeat = text.indexOf(pattern, textIdx + 1);
                        if (repeat >= 0) {
                            backtrack.push(new int[] {wcsIdx, repeat});
                        }
                    } else {
                        // matching from current position
                        if (!text.startsWith(pattern, textIdx)) {
                            // couldnt match token
                            break;
                        }
                    }

                    // matched text token, move text index to end of matched token
                    textIdx += pattern.length();
                    anyChars = false;
                }

                wcsIdx++;
            }

            // full match
            if (wcsIdx == wcs.length && textIdx == text.length()) {
                return true;
            }

        } while (backtrack.size() > 0);

        return false;
    }

    public String toString() {
        return "[WildcardMatcher{patterns:"+ Arrays.toString(rawPatterns)+"}]";
    }
}
