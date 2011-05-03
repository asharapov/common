package org.echosoft.common.cli.parser;

import java.io.PrintStream;

import org.echosoft.common.utils.StringUtil;

/**
 * @author Anton Sharapov
 */
public class CLPrintUtils {

    public static void printHelp(final Options options, final PrintStream out) {
        final String prefix = "  ";
        final String CRLF = "\n";
        final StringBuilder buf = new StringBuilder(4096);

        // вычисляем максимальную длину первой колонки (с названиями опций) ...
        final int prefixLength = prefix.length();
        int maxSize = prefixLength;
        for (Option opt : options.getOptions()) {
            if (opt.getShortName()!=null) {
                int size = prefixLength + 3;
                if (opt.hasArgs()) {
                    size += 6 + opt.getArgName().length();
                }
                maxSize = Math.max(maxSize, size);
            }
            if (opt.getFullName()!=null) {
                int size = prefixLength + 2 + opt.getFullName().length();
                if (opt.hasArgs()) {
                    size += 6 + opt.getArgName().length();
                }
                maxSize = Math.max(maxSize, size);
            }
        }

        // начинаем отрисовку описания опций ...
        for (Option opt : options.getOptions()) {
            if (opt.getShortName()!=null) {
                buf.append(prefix).append(" -").append(opt.getShortName());
                if (opt.hasArgs() && opt.getFullName()==null) {
                    buf.append(" = <").append(opt.getArgName()).append("> ");
                } else {
                    buf.append(CRLF);
                }
            }
            if (opt.getFullName()!=null) {
                buf.append(prefix).append("--").append(opt.getFullName());
                if (opt.hasArgs()) {
                    buf.append(" = <").append(opt.getArgName()).append("> ");
                }
            }

            final int d1 = buf.lastIndexOf("\n");
            final int len1 = d1>=0
                    ? maxSize - buf.toString().length() + d1 + 1
                    : maxSize - buf.toString().length() + 1;
            buf.append( StringUtil.leadRight("", ' ', len1) );

            if (opt.getDescription()!=null)
                buf.append(' ').append(opt.getDescription());
            buf.append(CRLF);
        }

        out.append(buf);
    }

}
