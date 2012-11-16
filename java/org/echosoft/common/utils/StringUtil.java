package org.echosoft.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.echosoft.common.io.FastStringTokenizer;

/**
 * Содержит часто используемые методы для работы со строками.
 *
 * @author Andrey Ochirov
 * @author Anton Sharapov.
 */
public class StringUtil {

    public static final String EMPTY_STRING_ARRAY[] = new String[0];
    private static final char[][] REPLACEMENT_HTML_TEXTS;
    private static final char[][] REPLACEMENT_HTML_ATTRS;
    static {
        REPLACEMENT_HTML_TEXTS = new char[128][];
        final char[] empty = new char[0];
        for (int i = 0; i < 32; i++) {
            REPLACEMENT_HTML_TEXTS[i] = empty;
        }
        REPLACEMENT_HTML_TEXTS['\t'] = null;
        REPLACEMENT_HTML_TEXTS['<'] = "&lt;".toCharArray();
        REPLACEMENT_HTML_TEXTS['>'] = "&gt;".toCharArray();
        REPLACEMENT_HTML_TEXTS['&'] = "&amp;".toCharArray();
        REPLACEMENT_HTML_ATTRS = REPLACEMENT_HTML_TEXTS.clone();
        REPLACEMENT_HTML_ATTRS['\"'] = "&quot;".toCharArray();
        REPLACEMENT_HTML_ATTRS['\''] = "&apos;".toCharArray();
        REPLACEMENT_HTML_TEXTS['\r'] = null;
        REPLACEMENT_HTML_TEXTS['\n'] = null;
    }


    private StringUtil() {
    }

    /**
     * Преобразовывает указанный в аргументе java объект в строку. Если аргумент содержит <code>null</code> то метод возвращает пустую строку
     * (в этом его единственное отличие от стандартного метода {@link String#valueOf(Object)} который в подобном случае возвращает строку <code>"null"</code>.
     *
     * @param obj объект который должен быть преобразован в строку.
     * @return Строковая форма переданного в аргументе объекта или строка нулевой длины если в аргументе передан <code>null</code>.
     */
    public static String valueOf(final Object obj) {
        return obj != null ? obj.toString() : "";
    }

    /**
     * Возвращает копию переданной в аргументе строки без начальных и завершающих пробелов (а также всех прочих символов с кодом меньше  <code>'&#92;u0020'</code>).
     * Если полученная строка имеет нулевую длину то метод возвращает <code>null</code>.
     *
     * @param string строка в которой надо избавиться от начальных и завершающих пробелов.
     * @return копия переданной в аргументе строки без начальных и завершающих пробелов или <code>null</code>.
     */
    public static String trim(String string) {
        if (string != null) {
            string = string.trim();
            if (string.length() < 1) {
                string = null;
            }
        }
        return string;
    }

    /**
     * Возвращает первый аргумент если он не null и не пустая строка, в противном случае возвращает второй аргумент.
     *
     * @param text        строковое значение
     * @param defaultText строка которая будет возвращать в случае если первым аргументом идет <code>null</code> или пустая строка.
     * @return первый аргумент если он не равен <code>null</code> и не является пустой строкой, в противном случае возвращается второй аргумент.
     */
    public static String getNonEmpty(final String text, final String defaultText) {
        return text == null || text.isEmpty() ? defaultText : text;
    }

    /**
     * Сравнивает строки, каждая из которых может быть <code>null</code>.
     *
     * @param str1 первая строка для сравнения.
     * @param str2 вторая строка для сравнения.
     * @return <ul>
     *         <li> <code>-1</code>  если первая строка меньше второй или только первая строка равна <code>null</code>.
     *         <li> <code>0</code>  если обе строки одинаковы или обе равны <code>null</code>.
     *         <li> <code>1</code>  если первая строка больше второй или только вторая строка равна <code>null</code>.
     *         </ul>
     */
    public static int compareNullableStrings(final String str1, final String str2) {
        if (str1 == null) {
            return str2 == null ? 0 : -1;
        } else {
            return str2 == null ? 1 : str1.compareTo(str2);
        }
    }


    /**
     * Дополняет переданную в аргументе строку до требуемой длины путем добавления в начало строки указанных символов.
     * Если длина исходной строки равна или больше указанной длины то метод не делает ничего и возвращает исходную строку.
     *
     * @param str            исходная строка
     * @param symbol         символ который используется до заполнения строки до требуемой длины.
     * @param requiredLength минимальная требуемая длина возвращаемой методом строки.
     * @return строка требуемой длины.
     */
    public static String leadLeft(final String str, final char symbol, final int requiredLength) {
        final char[] buf;
        final int strlen;
        if (str == null || (strlen = str.length()) == 0) {
            buf = new char[requiredLength];
            for (int i = 0; i < requiredLength; i++) buf[i] = symbol;
            return new String(buf);
        } else {
            final int mustBeAdded = requiredLength - strlen;
            if (mustBeAdded <= 0)
                return str;
            buf = new char[requiredLength];
            for (int i = 0; i < mustBeAdded; i++) buf[i] = symbol;
            str.getChars(0, strlen, buf, mustBeAdded);
            return new String(buf);
        }
    }

    /**
     * Дополняет переданную в аргументе строку до требуемой длины путем добавления в конец строки указанных символов.
     * Если длина исходной строки равна или больше указанной длины то метод не делает ничего и возвращает исходную строку.
     *
     * @param str            исходная строка
     * @param symbol         символ который используется до заполнения строки до требуемой длины.
     * @param requiredLength минимальная требуемая длина возвращаемой методом строки.
     * @return строка требуемой длины.
     */
    public static String leadRight(final String str, final char symbol, final int requiredLength) {
        final char[] buf;
        final int strlen;
        if (str == null || (strlen = str.length()) == 0) {
            buf = new char[requiredLength];
            for (int i = 0; i < requiredLength; i++) buf[i] = symbol;
            return new String(buf);
        } else {
            final int mustBeAdded = requiredLength - strlen;
            if (mustBeAdded <= 0)
                return str;
            buf = new char[requiredLength];
            str.getChars(0, strlen, buf, 0);
            for (int i = strlen; i < requiredLength; i++) buf[i] = symbol;
            return new String(buf);
        }
    }

    /**
     * В указанной строке начиная с определенной позиции ищет первое вхождение одного из перечисленных символов.
     *
     * @param string   строка в которой требуется найти первое вхождение одного из требуемых символов.
     * @param startPos неотрицательное число, определяет с какой позиции следует начинать поиск.
     * @param chars    непустой массив символов. Метод ищет первое вхождение одного из них.
     * @return позиция по которой находится один из перечисленных в аргументе символов или -1 если ни один символ в строке не найден.
     */
    public static int indexOf(final CharSequence string, final int startPos, final char... chars) {
        for (int i = startPos, len = string.length(); i < len; i++) {
            final char c = string.charAt(i);
            for (int j = chars.length - 1; j >= 0; j--) {
                if (c == chars[j])
                    return i;
            }
        }
        return -1;
    }

    /**
     * Возвращает начало строки переданной в аргументе до позиции первого вхождения в нее символа-разделителя (исключая его).
     *
     * @param text      исходная строка. Должна быть указана обязательно.
     * @param delimiter символ-разделитель.
     * @return Начало исходной строки до разделителя.
     *         Если в исходной строке символ-разделитель отсутствует, то возвращается вся строка целиком.
     *         Если в исходной строке символ-разделитель стоит первым символом то метод возвращает пустую строку.
     */
    public static String getHead(final String text, final char delimiter) {
        if (text == null)
            return null;
        final int p = text.indexOf(delimiter);
        return p >= 0 ? text.substring(0, p) : text;
    }

    /**
     * Возвращает окончание строки переданной в аргументе начиная с позиции, следующей за первым вхождением символа-разделителя.
     *
     * @param text      исходная строка. Должна быть указана обязательно.
     * @param delimiter символ-разделитель.
     * @return Окончание исходной строки начиная с позиции следующей за первым вхождением символа-разделителя.
     *         Если в исходной строке символ-разделитель отсутствует, то возвращается <code>null</code>.
     *         Если в исходной строке единственное вхождение символа-разделителя стоит последним символом то метод возвращает пустую строку.
     */
    public static String getTail(final String text, final char delimiter) {
        if (text == null)
            return null;
        final int p = text.indexOf(delimiter);
        return p >= 0 ? text.substring(p + 1) : null;
    }

    /**
     * Вырезает из строки все управляющие символы с ASCII кодами в диапазоне 0..31 включительно.
     *
     * @param text строка из которой должны быть вырезаны управляющие символы.
     * @return полученная в результате строка.
     */
    public static String skipControlChars(final CharSequence text) {
        if (text == null)
            return null;
        final int len = text.length();
        final StringBuilder buf = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            final char c = text.charAt(i);
            if (c >= 32)
                buf.append(c);
        }
        return buf.toString();
    }

    /**
     * Заменяет в строке все вхождения одной указанной подстроки на другую подстроку.
     *
     * @param text    текст в котором должна быть выполнена замена одной подстроки на другую. Не может быть <code>null</code>.
     * @param pattern подстрока которая должна быть заменена на другую. Не может быть <code>null</code>.
     * @param value   подстрока на которую должна быть заменена исходная подстрока.
     * @return результат замены одной подстроки на другую подстроку.
     */
    public static String replace(final String text, final String pattern, final String value) {
        if (text == null || pattern == null || value == null)
            return null;

        final int textSize = text.length();
        final int patternSize = pattern.length();
        int oldPos = 0;
        final StringBuilder result = new StringBuilder(textSize);
        for (int pos = text.indexOf(pattern, 0); pos >= 0; pos = text.indexOf(pattern, oldPos)) {
            result.append(text.substring(oldPos, pos));
            result.append(value);
            oldPos = pos + patternSize;
        }
        result.append(text.substring(oldPos, textSize));
        return result.toString();
    }

    /**
     * Заменяет все вхождения указанных в атрибуте <code>attrs</code> выражений на соответствующие им значения.
     *
     * @param text   строка в которой требуется провести серию замен. Если аргумент равен <code>null</code> то метод завершает работу возвращая <code>null</code>.
     * @param attrs  перечень шаблонов в строке и соответствующих им реальным значениям на которые эти шаблоны должны быть заменены. Если аргумент равен <code>null</code> то метод завершает работу возвращая исходную строку.
     * @param prefix специальный маркерный символ предшествующий началу очередного шаблона для замены. Не может быть пустым.
     * @param suffix специальный маркерный символ завершающий текст шаблона для замены. Не может быть пустым или быть идентичным префиксу.
     * @return Итоговая строка, в которой все шаблоны заменены соответствующими значениями.
     */
    public static String replace(final String text, final Map<String, CharSequence> attrs, final char prefix, final char suffix) {
        if (text == null || attrs == null)
            return text;
        final int textSize = text.length();
        final StringBuilder result = new StringBuilder(textSize);
        int pos = 0;
        for (int sp = text.indexOf(prefix, 0); sp >= 0; sp = text.indexOf(prefix, pos)) {
            int ep = text.indexOf(suffix, sp);
            if (ep < 0)
                break;
            for (int spp = text.indexOf(prefix, sp + 1); spp > 0 && spp < ep; spp = text.indexOf(prefix, sp + 1)) sp = spp;
            result.append(text.substring(pos, sp));
            final String key = text.substring(sp + 1, ep);
            result.append(attrs.get(key));
            pos = ep + 1;
        }
        result.append(text.substring(pos, textSize));
        return result.toString();
    }

    /**
     * Объединяет список строк в одну строку, используя в качестве разделителя между исходными частями ее специальный символ.
     * Как правило используется в паре с методом {@link StringUtil#split(char, char, String)}, выполняющим обратную операцию.
     * <table border="1" style="border:1px solid black; white-space:nowrap;">
     * <caption>Примеры использования</caption>
     * <tr><th colspan="3">Аргументы вызова</th><th rowspan="2">Результат</th></tr>
     * <tr><th>mask</th><th>separator</th><th>Объединяемые строки</th</tr>
     * <tr><td>'&'</td><td>':'</td><td>"aaa", "bbb", "ccc"</td><td>"aaa:bbb:ccc"</td></tr>
     * <tr><td>'&'</td><td>':'</td><td>"", "bbb", ""</td><td>":bbb:"</td></tr>
     * <tr><td>'&'</td><td>':'</td><td>"a:1", "b:2", "x&y"</td><td>"a&:1:b&:2:x&&y"</td></tr>
     * </table>
     *
     * @param mask      символ при помощи которого будет маскироваться символ-разделитель встречающийся в объединяемых строках.
     * @param separator символ, которым будут разделяться объединяемые строки в итоговой строке.
     * @param parts     массив объединяемых строк. Может быть пустым но ни одна из этих строк не может быть <code>null</code>.
     * @return Строка состоящая из объединяемых строк, разделенных символом-разделителем.
     */
    public static String join(final char mask, final char separator, final String... parts) {
        if (parts == null)
            return null;
        final StringBuilder buf = new StringBuilder(32);
        boolean first = true;
        for (String part : parts) {
            if (first) {
                first = false;
            } else {
                buf.append(separator);
            }
            for (int i = 0, len = part.length(); i < len; i++) {
                final char c = part.charAt(i);
                if (c == mask) {
                    buf.append(mask).append(mask);
                } else if (c == separator) {
                    buf.append(mask).append(separator);
                } else {
                    buf.append(c);
                }
            }
        }
        return buf.toString();
    }

    /**
     * Выполняет операцию, обратную той что делает метод {@link StringUtil#join(char, char, String...)}
     *
     * @param mask      символ которым маскируется символ-разделитель в строке, которую требуется разбить на подстроки.
     * @param separator символ, по которому будут разделяться подстроки в исходной строке.
     * @param text      Строка которую требуется разбить на подстроки. Может быть пустой строкой.
     * @return Массив строк полученных путем разбиения исходной строки на подстроки, где в качестве разделителя используется указанный во втором аргументе символ.
     *         Массив может быть пустым если исходная строка была нулевой длины. Метод возвращает <code>null</code> если и исходная строка была равна <code>null</code>.
     */
    public static String[] split(final char mask, final char separator, final String text) {
        if (text == null)
            return null;
        if (text.length() == 0)
            return EMPTY_STRING_ARRAY;
        final ArrayList<String> parts = new ArrayList<String>(5);
        final StringBuilder buf = new StringBuilder(32);
        boolean masked = false;
        for (int i = 0, len = text.length(); i < len; i++) {
            final char c = text.charAt(i);
            if (c == mask) {
                if (masked) {
                    buf.append(mask);
                    masked = false;
                } else {
                    masked = true;
                }
            } else if (c == separator) {
                if (masked) {
                    buf.append(separator);
                    masked = false;
                } else {
                    parts.add(buf.toString());
                    buf.setLength(0);
                }
            } else {
                buf.append(c);
            }
        }
        parts.add(buf.toString());
        return parts.toArray(new String[parts.size()]);
    }

    /**
     * Разбивает исходную строку на несколько подстрок трактуя указанный символ как разделитель.
     * Примеры использования метода когда в качестве подстроки используется символ '_' :
     * <ol>
     * <li> исходная строка <code>a_b_c</code> будет разбита на три подстроки: {"a", "b", "c"}.
     * <li> исходная строка <code>a_b_</code> будет разбита на две подстроки: {"a", "b"}.
     * <li> исходная строка <code>a__c</code> будет разбита на три подстроки: {"a", "", "c"}.
     * <li> исходная строка <code>_b_c</code> будет разбита на три подстроки: {"", "b", "c"}.
     * <li> исходная строка <code>a</code> будет оставлена как есть: {"a"}.
     * </ol>
     *
     * @param text      исходная строка.
     * @param separator символ используемый в качестве разделителя.
     * @return массив строк полученных в результате разделения исходной строки на подстроки используя указанный символ-разделитель.
     *         Метод возвращает <code>null</code> если исходная строка равна <code>null</code>.
     */
    public static String[] split(final String text, final char separator) {
        if (text == null)
            return null;
        final ArrayList<String> buf = new ArrayList<String>();
        final int length = text.length();
        int pos = 0;
        for (int i = 0; i < length; i++) {
            if (text.charAt(i) == separator) {
                buf.add(text.substring(pos, i));
                pos = i + 1;
            }
        }
        if (pos < length)
            buf.add(text.substring(pos, length));
        return buf.toArray(new String[buf.size()]);
    }

    /**
     * Разбивает исходную строку на несколько подстрок трактуя указанный символ как разделитель.
     * В полученных после разбиения подстроках удаляются концевые пробелы и если полученная подстрока будет не пустой то она будет включена в результат данной функции.
     * Примеры использования метода когда в качестве подстроки используется символ '_' :
     * <ol>
     * <li> исходная строка <code>a_b_c</code> будет разбита на три подстроки: {"a", "b", "c"}.
     * <li> исходная строка <code>a_b_</code> будет разбита на две подстроки: {"a", "b"}.
     * <li> исходная строка <code>a__c</code> будет разбита на три подстроки: {"a", "c"}.
     * <li> исходная строка <code>_ b _c</code> будет разбита на две подстроки: {"b", "c"}.
     * <li> исходная строка <code>a</code> будет оставлена как есть: {"a"}.
     * <li> исходная строка <code>__</code> будет представлена в виде пустого множества: {}.
     * <li> исходная строка <code>  </code></code> будет представлена в виде пустого множества: {}.
     * </ol>
     *
     * @param text      исходная строка.
     * @param separator символ используемый в качестве разделителя.
     * @return массив непустых строк полученных в результате разделения исходной строки на подстроки используя указанный символ-разделитель.
     *         Метод возвращает <code>null</code> если исходная строка равна <code>null</code>.
     */
    public static String[] splitIgnoringEmpty(final String text, final char separator) {
        if (text == null)
            return null;
        final ArrayList<String> buf = new ArrayList<String>();
        final int length = text.length();
        int pos = 0;
        for (int i = 0; i < length; i++) {
            if (text.charAt(i) == separator) {
                final String token = text.substring(pos, i).trim();
                if (!token.isEmpty())
                    buf.add(token);
                pos = i + 1;
            }
        }
        if (pos < length) {
            final String token = text.substring(pos, length).trim();
            if (!token.isEmpty())
                buf.add(token);
        }
        return buf.toArray(new String[buf.size()]);
    }

    /**
     * Маскирует использование символа указанного в аргументе <code>maskedSymbol</code> при помощи некоторого другого символа <code>maskingSymbol</code>.<br/>
     * пример: вызов метода: <b><code>mask("abc-def\iklmn",'-','\')</code></b>
     * вернет строку: <b><code>"abc\-def\\iklmn"</code></b>.
     *
     * @param text          исходная строка.
     * @param maskedSymbol  символ, использование которого требуется замаскировать в строке с использованием другого символа.
     * @param maskingSymbol символ, которым требуется замаскировать использование некоторого другого символа.
     * @return измененная исходная строка в которой перед каждым вхождением маскируемого символа <code>maskedSymbol</code> установлен маскирующий символ <code>maskingSymbol</code>.
     *         Если в исходной строке маскируемый символ не встречается то метод вернет исходную строку без изменений.
     */
    public static String mask(final String text, final char maskedSymbol, final char maskingSymbol) {
        if (text == null || text.isEmpty() || text.indexOf(maskedSymbol, 0) < 0 && text.indexOf(maskingSymbol, 0) < 0)
            return text;
        final int length = text.length();
        final StringBuilder buf = new StringBuilder(text.length() + 2);
        for (int i = 0; i < length; i++) {
            final char c = text.charAt(i);
            if (c == maskedSymbol || c == maskingSymbol) {
                buf.append(maskingSymbol);
            }
            buf.append(c);
        }
        return buf.toString();
    }

    /**
     * Выполняет операцию, обратную той что делает метод {@link StringUtil#mask(String, char, char)}, т.е всегда выполняется условие:<br/>
     * <code>StringUtil.unmask(StringUtil.mask(str,c,mask), mask) == str</code>.
     *
     * @param text          строка в которой требуется убрать маскировку с символов.
     * @param maskedSymbol  символ, использование которого маскировалось в строке с использованием другого символа.
     * @param maskingSymbol символ, используемый для маскировки других символов.
     * @return измененная исходная строка в которой была убрана маскировка
     */
    public static String unmask(final String text, final char maskedSymbol, final char maskingSymbol) {
        if (text == null || text.isEmpty() || text.indexOf(maskingSymbol, 0) < 0)
            return text;
        final int length = text.length();
        final StringBuilder buf = new StringBuilder(text.length());
        boolean masked = false;
        for (int i = 0; i < length; i++) {
            final char c = text.charAt(i);
            if (masked) {
                if (c != maskedSymbol && c != maskingSymbol) {
                    buf.append(maskingSymbol);
                }
                buf.append(c);
                masked = false;
            } else {
                if (c == maskingSymbol) {
                    masked = true;
                } else {
                    buf.append(c);
                }
            }
        }
        if (masked)
            buf.append(maskingSymbol);
        return buf.toString();
    }


    /**
     * Конвертирует недопустимые в HTML тексте символы в соответствующие кодовые обозначения принятые в HTML.<br/>
     * Заменяет символы '&', '<', '>' на соответствующие обозначения принятые в HTML: "&amp;amp;", "&amp;lt;", "&amp;gt;".
     *
     * @param text оригинальный текст HTML.
     * @return исходный текст в котором символы
     */
    public static String encodeXMLText(final CharSequence text) {
        final int length;
        if (text == null || (length = text.length()) == 0)
            return "";
        final StringBuilder dst = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char ch = text.charAt(i);
            final char[] replacement;
            if (ch >= 128 || (replacement = REPLACEMENT_HTML_TEXTS[ch]) == null) {
                dst.append(ch);
            } else {
                dst.append(replacement);
            }
        }
        return dst.toString();
    }

    /**
     * Конвертирует недопустимые в HTML тексте символы в соответствующие кодовые обозначения принятые в HTML.<br/>
     * Заменяет символы '&', '<', '>' на соответствующие обозначения принятые в HTML: "&amp;amp;", "&amp;lt;", "&amp;gt;".
     *
     * @param out  выходной поток куда будет помещена отконвертированная версия входной строки.
     * @param text оригинальный текст HTML.
     * @throws IOException в случае каких-либо проблем вывода данных в поток.
     */
    public static void encodeXMLText(final Writer out, final String text) throws IOException {
        final int length;
        if (text == null || (length = text.length()) == 0)
            return;
        int last = 0;
        for (int i = 0; i < length; i++) {
            final char ch = text.charAt(i);
            final char[] replacement;
            if (ch >= 128 || (replacement = REPLACEMENT_HTML_TEXTS[ch]) == null)
                continue;
            if (last < i)
                out.write(text, last, i - last);
            out.write(replacement);
            last = i + 1;
        }
        if (last < length)
            out.write(text, last, length - last);
    }

    /**
     * Конвертирует недопустимые в атрибутах тегов HTML символы в соответствующие кодовые обозначения принятые в HTML.<br/>
     * Заменяет символы '&', ' " ', ' ' ' на соответствующие обозначения принятые в HTML: "&amp;amp;", "&amp;quot;", "&amp;#39;".
     *
     * @param text оригинальный текст HTML.
     * @return исходный текст в котором символы
     */
    public static String encodeXMLAttribute(final CharSequence text) {
        final int length;
        if (text == null || (length = text.length()) == 0)
            return "";
        final StringBuilder dst = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char ch = text.charAt(i);
            final char[] replacement;
            if (ch >= 128 || (replacement = REPLACEMENT_HTML_ATTRS[ch]) == null) {
                dst.append(ch);
            } else {
                dst.append(replacement);
            }
        }
        return dst.toString();
    }

    /**
     * Конвертирует недопустимые в атрибутах тегов HTML символы в соответствующие кодовые обозначения принятые в HTML.<br/>
     * Заменяет символы '&', ' " ', ' ' ' на соответствующие обозначения принятые в HTML: "&amp;amp;", "&amp;quot;", "&amp;#39;".
     *
     * @param out   выходной поток куда будет помещена отконвертированная версия входной строки.
     * @param text оригинальный текст значения атрибута HTML.
     * @throws IOException в случае каких-либо проблем вывода данных в поток.
     */
    public static void encodeXMLAttribute(final Writer out, final String text) throws IOException {
        final int length;
        if (text == null || (length = text.length()) == 0)
            return;
        int last = 0;
        for (int i = 0; i < length; i++) {
            final char ch = text.charAt(i);
            final char[] replacement;
            if (ch >= 128 || (replacement = REPLACEMENT_HTML_ATTRS[ch]) == null)
                continue;
            //if (ch == '&' && (i + 1) < length && text.charAt(i + 1) == '{') continue;             // HTML spec B.7.1 (reserved syntax for future script macros)
            if (last < i)
                out.write(text, last, i - last);
            out.write(replacement);
            last = i + 1;
        }
        if (last < length)
            out.write(text, last, length - last);
    }


    @Deprecated
    public static String encodeHTMLText(final CharSequence text) {
        return encodeXMLText(text);
    }
    @Deprecated
    public static void encodeHTMLText(final Writer out, final String text) throws IOException {
        encodeXMLText(out, text);
    }
    @Deprecated
    public static String encodeHTMLAttribute(final CharSequence text) {
        return encodeXMLAttribute(text);
    }
    @Deprecated
    public static void encodeHTMLAttribute(final Writer out, final String text) throws IOException {
        encodeXMLAttribute(out, text);
    }

    /**
     * Преобразовывает строки вида <code>aa/bb/dd/../cc</code> в строки вида <code>aa/bb/cc</code>.<br/>
     * Данный метод может использоваться при вычисления абсолютных путей до файлов, ресурсов в ClassPath или ссылок на ресурсы в Web.
     * <table border="1" style="border:1px solid black; white-space:nowrap;">
     * <caption>Примеры использования</caption>
     * <tr><th colspan="2">Аргументы вызова</th><th rowspan="2">Результат</th></tr>
     * <tr><th>Контекст</th><th>Путь</th></tr>
     * <tr><td>/mycontext</td><td>null</td><td>/mycontext</td></tr>
     * <tr><td>/mycontext</td><td></td><td>/mycontext</td></tr>
     * <tr><td>/mycontext</td><td>/</td><td>/</td></tr>
     * <tr><td>/mycontext</td><td>/aa/bb/cc</td><td>/aa/bb/cc</td></tr>
     * <tr><td>/mycontext</td><td>/aa//bb/./cc/</td><td>/aa/bb/cc/</td></tr>
     * <tr><td>/mycontext</td><td>/aa/bb/../cc</td><td>/aa/cc</td></tr>
     * <tr><td>/mycontext</td><td>aa/bb/../cc</td><td>/mycontext/aa/cc</td></tr>
     * <tr><td>/mycontext</td><td>./aa/bb/../cc</td><td>/mycontext/aa/cc</td></tr>
     * </table>
     *
     * @param context контекст, относительно которого указывается во втором аргументе нормализуемый путь к ресурсу.
     *                Данный параметр используется исключительно в тех случаях когда путь НЕ начинается с символа '<code>/</code>'
     *                Значение данного аргемента не может быть <code>null</code> а также не может содержать символы, не допустимые в пути URL.
     * @param path    требующий нормализации путь к некоторому ресурсу. В качестве разделителя между элементами пути используется символ '<code>/</code>'.
     * @return нормализованный путь к некоторому ресурсу.
     * @throws IndexOutOfBoundsException В случае некорректно составленного url за счет избыточного использования токенов <code>..</code>
     */
    public static String normalizePath(final String context, final String path) {
        if (path == null)
            return context;
        final ArrayList<String> tokens = new ArrayList<String>(10);
        boolean first = true, addContext = true;
        for (Iterator<String> it = new FastStringTokenizer(path.trim(), '/', (char) 0); it.hasNext(); ) {
            final String token = it.next();
            if (token.length() == 0) {
                if (first)
                    addContext = false;
            } else if (".".equals(token)) {
            } else if ("..".equals(token)) {
                tokens.remove(tokens.size() - 1);
            } else {
                tokens.add(token);
            }
            first = false;
        }
        final StringBuilder buf = new StringBuilder(32);
        if (addContext) {
            if (tokens.size() > 0 && context.endsWith("/")) {
                buf.append(context.substring(0, context.length() - 1));
            } else {
                buf.append(context);
            }
        } else {
            if (tokens.size() == 0)
                return "/";
        }
        for (String token : tokens) {
            buf.append('/');
            buf.append(token);
        }
        return buf.toString();
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>dd.MM.yyyy</code>.
     *
     * @param date дата которую требуется преобразовать в строку.
     * @return результат форматирования даты или пустая строка если исходная дата равна <code>null</code>.
     */
    public static String formatDate(final Date date) {
        if (date == null)
            return "";
        final StringBuilder buf = new StringBuilder(10);
        final Calendar cal = getCalendarInstanceForThread();
        cal.setTime(date);
        int p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(p);
        buf.append('.');
        p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(p);
        buf.append('.');
        buf.append(cal.get(Calendar.YEAR));
        return buf.toString();
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>dd.MM.yyyy</code>.
     * Если исходная дата равна <code>null</code> то метод не делает ничего.
     *
     * @param buf  буффер, реализующий интерфейс {@link Appendable} в котором будет аккумулироваться результат.
     * @param date дата которую требуется преобразовать в строку.
     * @throws IOException в случае каких-либо проблем с сохранением данных в буфере.
     */
    public static void formatDate(final Appendable buf, final Date date) throws IOException {
        if (date == null)
            return;
        final Calendar cal = getCalendarInstanceForThread();
        cal.setTime(date);
        int p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('.');
        p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('.');
        buf.append(Integer.toString(cal.get(Calendar.YEAR)));
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>dd.MM.yyyy</code>.
     * Если исходная дата равна <code>null</code> то метод не делает ничего.
     *
     * @param buf  буффер, реализующий интерфейс {@link Appendable} в котором будет аккумулироваться результат.
     * @param date дата которую требуется преобразовать в строку.
     */
    public static void formatDate(final StringBuilder buf, final Date date) {
        if (date == null)
            return;
        final Calendar cal = getCalendarInstanceForThread();
        cal.setTime(date);
        int p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('.');
        p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('.');
        buf.append(Integer.toString(cal.get(Calendar.YEAR)));
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>dd.MM.yyyy HH:mm:ss</code>.
     *
     * @param date дата которую требуется преобразовать в строку.
     * @return результат форматирования даты или пустая строка если исходная дата равна <code>null</code>.
     */
    public static String formatDateTime(final Date date) {
        if (date == null)
            return "";
        final StringBuilder buf = new StringBuilder(19);
        final Calendar cal = getCalendarInstanceForThread();
        cal.setTime(date);
        int p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(p);
        buf.append('.');
        p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(p);
        buf.append('.');
        buf.append(cal.get(Calendar.YEAR));
        buf.append(' ');
        p = cal.get(Calendar.HOUR_OF_DAY);
        if (p < 10)
            buf.append('0');
        buf.append(p);
        buf.append(':');
        p = cal.get(Calendar.MINUTE);
        if (p < 10)
            buf.append('0');
        buf.append(p);
        buf.append(':');
        p = cal.get(Calendar.SECOND);
        if (p < 10)
            buf.append('0');
        buf.append(p);
        return buf.toString();
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>dd.MM.yyyy HH:mm:ss</code>.
     * Если исходная дата равна <code>null</code> то метод не делает ничего.
     *
     * @param buf  буффер, реализующий интерфейс {@link Appendable} в котором будет аккумулироваться результат.
     * @param date дата которую требуется преобразовать в строку.
     * @throws IOException в случае каких-либо проблем с сохранением данных в буфере.
     */
    public static void formatDateTime(final Appendable buf, final Date date) throws IOException {
        if (date == null)
            return;
        final Calendar cal = getCalendarInstanceForThread();
        cal.setTime(date);
        int p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('.');
        p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('.');
        buf.append(Integer.toString(cal.get(Calendar.YEAR)));
        buf.append(' ');
        p = cal.get(Calendar.HOUR_OF_DAY);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.MINUTE);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.SECOND);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>dd.MM.yyyy HH:mm:ss</code>.
     * Если исходная дата равна <code>null</code> то метод не делает ничего.
     *
     * @param buf  буффер, реализующий интерфейс {@link Appendable} в котором будет аккумулироваться результат.
     * @param date дата которую требуется преобразовать в строку.
     */
    public static void formatDateTime(final StringBuilder buf, final Date date) {
        if (date == null)
            return;
        final Calendar cal = getCalendarInstanceForThread();
        cal.setTime(date);
        int p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('.');
        p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('.');
        buf.append(Integer.toString(cal.get(Calendar.YEAR)));
        buf.append(' ');
        p = cal.get(Calendar.HOUR_OF_DAY);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.MINUTE);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.SECOND);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>dd.MM.yyyy HH:mm</code>.
     *
     * @param date дата которую требуется преобразовать в строку.
     * @return результат форматирования даты или пустая строка если исходная дата равна <code>null</code>.
     */
    public static String formatDateTime2(final Date date) {
        if (date == null)
            return "";
        final StringBuilder buf = new StringBuilder(16);
        final Calendar cal = getCalendarInstanceForThread();
        cal.setTime(date);
        int p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(p);
        buf.append('.');
        p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(p);
        buf.append('.');
        buf.append(cal.get(Calendar.YEAR));
        buf.append(' ');
        p = cal.get(Calendar.HOUR_OF_DAY);
        if (p < 10)
            buf.append('0');
        buf.append(p);
        buf.append(':');
        p = cal.get(Calendar.MINUTE);
        if (p < 10)
            buf.append('0');
        buf.append(p);
        return buf.toString();
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>dd.MM.yyyy HH:mm</code>.
     * Если исходная дата равна <code>null</code> то метод не делает ничего.
     *
     * @param buf  буффер, реализующий интерфейс {@link Appendable} в котором будет аккумулироваться результат.
     * @param date дата которую требуется преобразовать в строку.
     * @throws IOException в случае каких-либо проблем с сохранением данных в буфере.
     */
    public static void formatDateTime2(final Appendable buf, final Date date) throws IOException {
        if (date == null)
            return;
        final Calendar cal = getCalendarInstanceForThread();
        cal.setTime(date);
        int p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('.');
        p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('.');
        buf.append(Integer.toString(cal.get(Calendar.YEAR)));
        buf.append(' ');
        p = cal.get(Calendar.HOUR_OF_DAY);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.MINUTE);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>dd.MM.yyyy HH:mm</code>.
     * Если исходная дата равна <code>null</code> то метод не делает ничего.
     *
     * @param buf  буффер, реализующий интерфейс {@link Appendable} в котором будет аккумулироваться результат.
     * @param date дата которую требуется преобразовать в строку.
     */
    public static void formatDateTime2(final StringBuilder buf, final Date date) {
        if (date == null)
            return;
        final Calendar cal = getCalendarInstanceForThread();
        cal.setTime(date);
        int p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('.');
        p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('.');
        buf.append(Integer.toString(cal.get(Calendar.YEAR)));
        buf.append(' ');
        p = cal.get(Calendar.HOUR_OF_DAY);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.MINUTE);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>yyyy-MM-dd</code>.
     *
     * @param date дата которую требуется преобразовать в строку.
     * @return результат форматирования даты или пустая строка если исходная дата равна <code>null</code>.
     */
    public static String formatISODate(final Date date) {
        if (date == null)
            return "";
        final StringBuilder buf = new StringBuilder(10);
        final Calendar cal = getCalendarInstanceForThread();
        cal.setTime(date);
        buf.append(cal.get(Calendar.YEAR));
        buf.append('-');
        int p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(p);
        buf.append('-');
        p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(p);
        return buf.toString();
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>yyyy-MM-dd</code>.
     * Если исходная дата равна <code>null</code> то метод не делает ничего.
     *
     * @param buf  буффер, реализующий интерфейс {@link Appendable} в котором будет аккумулироваться результат.
     * @param date дата которую требуется преобразовать в строку.
     * @throws IOException в случае каких-либо проблем с сохранением данных в буфере.
     */
    public static void formatISODate(final Appendable buf, final Date date) throws IOException {
        if (date == null)
            return;
        final Calendar cal = getCalendarInstanceForThread();
        cal.setTime(date);
        buf.append(Integer.toString(cal.get(Calendar.YEAR)));
        buf.append('-');
        int p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('-');
        p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>yyyy-MM-dd</code>.
     * Если исходная дата равна <code>null</code> то метод не делает ничего.
     *
     * @param buf  буффер, реализующий интерфейс {@link Appendable} в котором будет аккумулироваться результат.
     * @param date дата которую требуется преобразовать в строку.
     */
    public static void formatISODate(final StringBuilder buf, final Date date) {
        if (date == null)
            return;
        final Calendar cal = getCalendarInstanceForThread();
        cal.setTime(date);
        buf.append(Integer.toString(cal.get(Calendar.YEAR)));
        buf.append('-');
        int p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('-');
        p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>yyyy-MM-ddTHH:mm:ss</code>.
     * Если исходная дата равна <code>null</code> то метод возвращает пустую строку.
     *
     * @param date дата которую требуется преобразовать в строку.
     * @return результат форматирования даты или пустая строка если исходная дата равна <code>null</code>.
     */
    public static String formatISODateTime(final Date date) {
        if (date == null)
            return "";
        final Calendar cal = getCalendarInstanceForThread();
        cal.setTime(date);
        final StringBuilder buf = new StringBuilder(19);
        buf.append(Integer.toString(cal.get(Calendar.YEAR)));
        buf.append('-');
        int p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('-');
        p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('T');
        p = cal.get(Calendar.HOUR_OF_DAY);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.MINUTE);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.SECOND);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        return buf.toString();
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>yyyy-MM-ddTHH:mm:ss</code>.
     * Если исходная дата равна <code>null</code> то метод не делает ничего.
     *
     * @param buf  буффер, реализующий интерфейс {@link Appendable} в котором будет аккумулироваться результат.
     * @param date дата которую требуется преобразовать в строку.
     * @throws IOException в случае каких-либо проблем с сохранением данных в буфере.
     */
    public static void formatISODateTime(final Appendable buf, final Date date) throws IOException {
        if (date == null)
            return;
        final Calendar cal = getCalendarInstanceForThread();
        cal.setTime(date);
        buf.append(Integer.toString(cal.get(Calendar.YEAR)));
        buf.append('-');
        int p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('-');
        p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('T');
        p = cal.get(Calendar.HOUR_OF_DAY);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.MINUTE);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.SECOND);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
    }

    /**
     * Преобразует переданную в аргументе дату в строку формата <code>yyyy-MM-ddTHH:mm:ss</code>.
     * Если исходная дата равна <code>null</code> то метод не делает ничего.
     *
     * @param buf  буффер, реализующий интерфейс {@link Appendable} в котором будет аккумулироваться результат.
     * @param date дата которую требуется преобразовать в строку.
     */
    public static void formatISODateTime(final StringBuilder buf, final Date date) {
        if (date == null)
            return;
        final Calendar cal = getCalendarInstanceForThread();
        cal.setTime(date);
        buf.append(Integer.toString(cal.get(Calendar.YEAR)));
        buf.append('-');
        int p = cal.get(Calendar.MONTH) + 1;
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('-');
        p = cal.get(Calendar.DAY_OF_MONTH);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append('T');
        p = cal.get(Calendar.HOUR_OF_DAY);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.MINUTE);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
        buf.append(':');
        p = cal.get(Calendar.SECOND);
        if (p < 10)
            buf.append('0');
        buf.append(Integer.toString(p));
    }


    /**
     * Осуществляет разбор даты из строки даты в формате <code>dd.MM.yyyy</code>. Если исходная строка равна null или имеет нулевую длину то метод вернет <code>null</code>.
     *
     * @param text строка в формате <code>dd.MM.yyyy</code>.
     * @return разобранную из строки дату или <code>null</code> если строковой аргумент равен <code>null</code> или имеет нулевую длину.
     * @throws ParseException в случае несоответствия переданной в аргументе строки требуемому формату.
     */
    public static Date parseDate(final CharSequence text) throws ParseException {
        final int length;
        if (text == null || (length = text.length()) == 0)
            return null;
        if (length < 10)
            throw new ParseException("length of the string in argument must be at least 10", 0);
        final Calendar cal = getCalendarInstanceForThread();
        cal.clear();
        if (text.charAt(2) != '.')
            throw new ParseException("Delimiter not finded", 2);
        if (text.charAt(5) != '.')
            throw new ParseException("Delimiter not finded", 5);
        cal.set(Calendar.DAY_OF_MONTH, parseInt(text, 0, 2));
        cal.set(Calendar.MONTH, parseInt(text, 3, 5) - 1);
        cal.set(Calendar.YEAR, parseInt(text, 6, 10));
        return cal.getTime();
    }

    /**
     * Осуществляет разбор даты из строки даты в формате <code>dd.MM.yyyy HH:mm:ss</code>. Если исходная строка равна null или имеет нулевую длину то метод вернет <code>null</code>.
     *
     * @param text строка в формате <code>dd.MM.yyyy HH:mm:ss</code>.
     * @return разобранную из строки дату или <code>null</code> если строковой аргумент равен <code>null</code> или имеет нулевую длину.
     * @throws ParseException в случае несоответствия переданной в аргументе строки требуемому формату.
     */
    public static Date parseDateTime(final CharSequence text) throws ParseException {
        final int length;
        if (text == null || (length = text.length()) == 0)
            return null;
        if (length < 19)
            throw new ParseException("length of the string in argument must be at least 19", 0);
        final Calendar cal = getCalendarInstanceForThread();
        cal.clear();
        if (text.charAt(2) != '.')
            throw new ParseException("Delimiter not finded", 2);
        if (text.charAt(5) != '.')
            throw new ParseException("Delimiter not finded", 5);
        if (text.charAt(10) != ' ')
            throw new ParseException("Delimiter not finded", 10);
        if (text.charAt(13) != ':')
            throw new ParseException("Delimiter not finded", 13);
        if (text.charAt(16) != ':')
            throw new ParseException("Delimiter not finded", 16);
        cal.set(Calendar.DAY_OF_MONTH, parseInt(text, 0, 2));
        cal.set(Calendar.MONTH, parseInt(text, 3, 5) - 1);
        cal.set(Calendar.YEAR, parseInt(text, 6, 10));
        cal.set(Calendar.HOUR_OF_DAY, parseInt(text, 11, 13));
        cal.set(Calendar.MINUTE, parseInt(text, 14, 16));
        cal.set(Calendar.SECOND, parseInt(text, 17, 19));
        return cal.getTime();
    }

    /**
     * Осуществляет разбор даты из строки даты в формате <code>dd.MM.yyyy HH:mm</code>. Если исходная строка равна null или имеет нулевую длину то метод вернет <code>null</code>.
     *
     * @param text строка в формате <code>dd.MM.yyyy HH:mm</code>.
     * @return разобранную из строки дату или <code>null</code> если строковой аргумент равен <code>null</code> или имеет нулевую длину.
     * @throws ParseException в случае несоответствия переданной в аргументе строки требуемому формату.
     */
    public static Date parseDateTime2(final CharSequence text) throws ParseException {
        final int length;
        if (text == null || (length = text.length()) == 0)
            return null;
        if (length < 16)
            throw new ParseException("length of the string in argument must be at least 16", 0);
        final Calendar cal = getCalendarInstanceForThread();
        cal.clear();
        if (text.charAt(2) != '.')
            throw new ParseException("Delimiter not finded", 2);
        if (text.charAt(5) != '.')
            throw new ParseException("Delimiter not finded", 5);
        if (text.charAt(10) != ' ')
            throw new ParseException("Delimiter not finded", 10);
        if (text.charAt(13) != ':')
            throw new ParseException("Delimiter not finded", 13);
        cal.set(Calendar.DAY_OF_MONTH, parseInt(text, 0, 2));
        cal.set(Calendar.MONTH, parseInt(text, 3, 5) - 1);
        cal.set(Calendar.YEAR, parseInt(text, 6, 10));
        cal.set(Calendar.HOUR_OF_DAY, parseInt(text, 11, 13));
        cal.set(Calendar.MINUTE, parseInt(text, 14, 16));
        return cal.getTime();
    }

    /**
     * Осуществляет разбор даты из строки даты в формате <code>yyyy-MM-dd</code>. Если исходная строка равна null или имеет нулевую длину то метод вернет <code>null</code>.
     *
     * @param text строка в формате <code>yyyy-MM-dd</code>.
     * @return разобранную из строки дату или <code>null</code> если строковой аргумент равен <code>null</code> или имеет нулевую длину.
     * @throws ParseException в случае несоответствия переданной в аргументе строки требуемому формату.
     */
    public static Date parseISODate(final CharSequence text) throws ParseException {
        final int length;
        if (text == null || (length = text.length()) == 0)
            return null;
        if (length < 10)
            throw new ParseException("length of the string in argument must be at least 10", 0);
        final Calendar cal = getCalendarInstanceForThread();
        cal.clear();
        if (text.charAt(4) != '-')
            throw new ParseException("Delimiter not finded", 4);
        if (text.charAt(7) != '-')
            throw new ParseException("Delimiter not finded", 7);
        cal.set(Calendar.YEAR, parseInt(text, 0, 4));
        cal.set(Calendar.MONTH, parseInt(text, 5, 7) - 1);
        cal.set(Calendar.DAY_OF_MONTH, parseInt(text, 8, 10));
        return cal.getTime();
    }

    /**
     * Осуществляет разбор даты из строки даты в одном из форматов: <code>yyyy-MM-dd HH:mm:ss</code>, <code>yyyy-MM-dd'T'HH:mm:ss</code>.
     * Если исходная строка равна null или имеет нулевую длину то метод вернет <code>null</code>.
     *
     * @param text строка в формате <code>yyyy-MM-dd HH:mm:ss</code> или <code>yyyy-MM-dd'T'HH:mm:ss</code>.
     * @return разобранную из строки дату или <code>null</code> если строковой аргумент равен <code>null</code> или имеет нулевую длину.
     * @throws ParseException в случае несоответствия переданной в аргументе строки требуемому формату.
     */
    public static Date parseISODateTime(final CharSequence text) throws ParseException {
        final int length;
        if (text == null || (length = text.length()) == 0)
            return null;
        if (length < 19)
            throw new ParseException("length of the string in argument must be at least 19", 0);
        final Calendar cal = getCalendarInstanceForThread();
        cal.clear();
        if (text.charAt(4) != '-')
            throw new ParseException("Delimiter not finded", 4);
        if (text.charAt(7) != '-')
            throw new ParseException("Delimiter not finded", 7);
        final char dt = text.charAt(10);
        if (dt != ' ' && dt != 'T')
            throw new ParseException("Delimiter not finded", 10);
        if (text.charAt(13) != ':')
            throw new ParseException("Delimiter not finded", 13);
        if (text.charAt(16) != ':')
            throw new ParseException("Delimiter not finded", 16);
        cal.set(Calendar.YEAR, parseInt(text, 0, 4));
        cal.set(Calendar.MONTH, parseInt(text, 5, 7) - 1);
        cal.set(Calendar.DAY_OF_MONTH, parseInt(text, 8, 10));
        cal.set(Calendar.HOUR_OF_DAY, parseInt(text, 11, 13));
        cal.set(Calendar.MINUTE, parseInt(text, 14, 16));
        cal.set(Calendar.SECOND, parseInt(text, 17, 19));
        return cal.getTime();
    }

    /**
     * Из строки содержащей полное имя класса (включающее полное имя пакета и имя класса в пакете) выделяет подстроку с именем класса.
     *
     * @param className строка содержащяя полное имя пакета и класса в пакете.
     * @return имя класса в пакете или "" если входной аргумент равен <code>null</code>.
     */
    public static String extractClass(final String className) {
        if (className == null)
            return "";
        final int p = className.lastIndexOf('.');
        return p >= 0 ? className.substring(p + 1) : className;
    }

    /**
     * Из строки содержащей полное имя класса (включающее полное имя пакета и имя класса в пакете) выделяет подстроку с полным именем пакета.
     *
     * @param className строка содержащяя полное имя пакета и класса в пакете.
     * @return имя класса в пакете или "" если входной аргумент равен <code>null</code>.
     */
    public static String extractPackage(final String className) {
        if (className == null)
            return "";
        final int p = className.lastIndexOf('.');
        return p >= 0 ? className.substring(0, p) : "";
    }

    /**
     * Возвращает строку со стеком вызова в случае возникновения исключительной ситуации.
     *
     * @param th исключение для которого надо вернуть строку со стеком вызова.
     * @return стек вызова.
     */
    public static String stackTrace(final Throwable th) {
        try {
            final ByteArrayOutputStream buf = new ByteArrayOutputStream(128);
            th.printStackTrace(new PrintWriter(buf, true));
            return buf.toString();
        } catch (Exception e) {
            return "Runtime error: " + e.getMessage();
        }
    }

    /**
     * Возвращает <code>true</code> если переданная в аргументе строка содержит только цифры.
     *
     * @param str строка
     * @return <code>false</code> если переданная в аргументе строка содержит какие-либо символы не являющиеся цифрами. Для пустой строки или <code>null</code> метод возвращает <code>true</code>.
     */
    public static boolean hasDigitsOnly(final CharSequence str) {
        if (str == null)
            return true;
        final int length = str.length();
        for (int i = length - 1; i >= 0; i--) {
            if (!Character.isDigit(str.charAt(i)))
                return false;
        }
        return true;
    }

    /**
     * Возвращает <code>true</code> если переданная в аргументе строка удовлетворяет требованиям предъявляемым
     * к идентификаторам в языке java.
     *
     * @param str строка.
     * @return <code>true</code> если переданная в аргументе строка удовлетворяет требованиям предъявляемым
     *         к идентификаторам в языке java.
     */
    public static boolean isJavaIdentifier(final CharSequence str) {
        if (str == null || str.length() == 0)
            return false;
        if (!Character.isJavaIdentifierStart(str.charAt(0)))
            return false;
        for (int i = 1, len = str.length(); i < len; i++) {
            if (!Character.isJavaIdentifierPart(str.charAt(i)))
                return false;
        }
        return true;
    }

    public static Set<String> asUnmodifiableSet(final String... items) {
        final Set<String> set = new HashSet<String>(items.length);
        set.addAll(Arrays.asList(items));
        return Collections.unmodifiableSet(set);
    }

    public static Set<String> asUnmodifiableSet(final Collection<String> items1, final String... items2) {
        final int length = items1 != null ? items1.size() + items2.length : items2.length;
        final Set<String> set = new HashSet<String>(length);
        set.addAll(items1);
        set.addAll(Arrays.asList(items2));
        return Collections.unmodifiableSet(set);
    }


    private static int parseInt(final CharSequence text, final int beginIndex, final int endIndex) throws ParseException {
        int result = 0;
        for (int i = beginIndex; i < endIndex; i++) {
            result *= 10;
            final int d = Character.digit(text.charAt(i), 10);
            if (d < 0)
                throw new ParseException("Invalid number format", i);
            result += d;
        }
        return result;
    }

    /**
     * Возвращает некогда ранее созданный экземпляр класса {@link Calendar} с неопределенным на момент вызова этого метода значением.<br/>
     * Единственное (и самое главное!) что гарантирует данный метод это то что возвращаемый объект можно безопасно  использовать в текущем потоке (и только в нем!).<br/>
     * Данный метод используется в целях избежания потерь на избыточном создании новых экземпляров {@link Calendar} так как этот класс не является потокобезопасным
     * и инициализация объектов этого класса занимает довольно большое время.
     *
     * @return экземпляр класса {@link Calendar} с неопределенными значениями своих свойств.
     */
    private static Calendar getCalendarInstanceForThread() {
        Calendar result = _ctl.get();
        if (result == null) {
            result = Calendar.getInstance();
            _ctl.set(result);
        }
        return result;
    }
    private static final ThreadLocal<Calendar> _ctl = new ThreadLocal<Calendar>();
}
