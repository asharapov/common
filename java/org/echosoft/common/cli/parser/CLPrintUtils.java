package org.echosoft.common.cli.parser;

import java.io.PrintStream;

import org.echosoft.common.utils.StringUtil;

/**
 * @author Anton Sharapov
 */
public class CLPrintUtils {

    public static void printHelp(final Options options, final PrintStream out) {
        printHelp(options, out, 0);
    }

    public static void printHelp(final Options options, final PrintStream out, int width) {
        final String prefix = "  ";
        final String CRLF = "\n";
        final StringBuilder buf = new StringBuilder(4096);

        // вычисляем максимальную длину первой колонки (с названиями опций) ...
        final int prefixLength = prefix.length();
        int maxSize = prefixLength;
        for (Option opt : options.getOptions()) {
            int size = prefixLength + 4;
            if (opt.getFullName() != null) {
                size += 2 + opt.getFullName().length();
            }
            if (opt.hasArgs()) {
                size += 5 + opt.getArgName().length();
            }
            size++;
            maxSize = Math.max(maxSize, size);
        }

        // начинаем отрисовку описания опций ...
        for (Option opt : options.getOptions()) {
            final StringBuilder ob = new StringBuilder(maxSize);
            ob.append(prefix);
            if (opt.getShortName() != null) {
                ob.append('-').append(opt.getShortName());
                if (opt.getFullName() != null) {
                    ob.append(", ");
                }
            } else {
                ob.append("    ");
            }
            if (opt.getFullName() != null) {
                ob.append("--").append(opt.getFullName());
            }
            if (opt.hasArgs()) {
                ob.append(" = <").append(opt.getArgName()).append(">");
            }
            ob.append(' ');

            while (ob.length() < maxSize)
                ob.append(' ');

            if (opt.getDescription() != null) {
                width = width > maxSize ? width : Integer.MAX_VALUE;
                for (int i = 0, l = opt.getDescription().length(); i < l; ) {
                    if (i > 0) {
                        for (int j = 0; j < maxSize; j++)
                            ob.append(' ');
                    }
                    final int end = i + width - maxSize;
                    ob.append(end < l ? opt.getDescription().substring(i, end) : opt.getDescription().substring(i));
                    ob.append(CRLF);
                    i = end;
                }
            } else {
                ob.append(CRLF);
            }
            buf.append(ob);
        }

        out.append(buf);
    }
}
