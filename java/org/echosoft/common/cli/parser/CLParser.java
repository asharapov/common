package org.echosoft.common.cli.parser;

import org.echosoft.common.collections.ObjectArrayIterator;
import org.echosoft.common.collections.ReadAheadIterator;

/**
 * Отвечает за разбор аргументов командной строки.
 *
 * @author Anton Sharapov
 */
public class CLParser {

    private final Options options;
    private final boolean stopAtNonOption;

    /**
     * Создает новый экземпляр парсера аргументов командной строки
     *
     * @param options         список допустимых в командной строке опций.
     * @param stopAtNonOption определяет поведение парсера при встрече нераспознанного токена.
     *                        Если <code>true</code> то метод поднимает исключение, в противном случае все нераспознанные токены сохраняются
     *                        в свойстве <code>unresolvedTokens</code> объекта {@link CommandLine} являющегося результатом разбора аргументов командной строки.
     */
    public CLParser(final Options options, final boolean stopAtNonOption) {
        this.options = options;
        this.stopAtNonOption = stopAtNonOption;
    }

    /**
     * @param args аргументы командной строки которые должны быть разобраны на список опций и их значений.
     * @return объект {@link CommandLine} с результатом разбора аргументов.
     * @throws CLParserException в случае каких-либо проблем при разборе.
     */
    public CommandLine parse(final String[] args) throws CLParserException {
        final CommandLine cmd = new CommandLine(options);
        for (ReadAheadIterator<String> it = new ObjectArrayIterator<String>(args); it.hasNext();) {
            final String token = it.next();

            if (!token.startsWith("-") || "-".equals(token)) {
                processUnknownToken(cmd, token, it);
                continue;
            }
            if ("--".equals(token)) {
                while (it.hasNext()) {
                    cmd.addUnresolvedArg(it.next());
                }
                break;
            }

            if (token.startsWith("--")) {
                // обрабатываем варианты --arg  или --arg=value
                final int eqp = token.indexOf('=');
                final Option opt = options.getOption( eqp>=0 ? token.substring(2,eqp) : token.substring(2) );
                final String value = eqp>=0 ? token.substring(eqp+1) : null;
                if (opt==null) {
                    processUnknownToken(cmd, token, it);
                } else
                if (value!=null) {
                    cmd.setOptionValue(opt, value);
                } else
                if (opt.hasArgs()) {
                    processOptionValue(cmd, opt, it);
                } else {
                    cmd.setOption(opt);
                }
            } else
            if (token.length() > 2) {
                // обрабатываем вариант -abcd
                for (int i = 1; i < token.length(); i++) {
                    final Option opt = options.getOption(token.charAt(i));
                    if (opt == null) {
                        processUnknownToken(cmd, '-' + token.substring(i), it);
                    } else
                    if (opt.isArgsRequired()) {
                        throw new MissingArgumentException(token.charAt(i));
                    } else {
                        cmd.setOption(opt);
                    }
                }
            } else {
                // обрабатываем вариант -a
                final Option opt = options.getOption(token.charAt(1));
                if (opt == null) {
                    processUnknownToken(cmd, token, it);
                } else
                if (opt.hasArgs()) {
                    processOptionValue(cmd, opt, it);
                } else {
                    cmd.setOption(opt);
                }
            }
        }

        // проверим наличие обязательных опций и обязательных значений опций ...
        for (Option opt : options.getOptions()) {
            if (opt.isRequired() && !cmd.hasOption(opt))
                throw new MissingOptionException(opt);
        }
        for (Option opt : cmd.getOptions()) {
            if (opt.isArgsRequired() && cmd.getOptionValue(opt, null) == null)
                throw new MissingArgumentException(opt);
        }
        return cmd;
    }

    protected void processOptionValue(final CommandLine cmd, final Option option, final ReadAheadIterator<String> it) throws CLParserException {
        if (!it.hasNext()) {
            cmd.setOption(option);
            return;
        }
        final String nextToken = it.readAhead();

        if (nextToken.startsWith("-") && nextToken.length()>1 ) {
            cmd.setOption(option);
        } else {
            cmd.setOptionValue(option, it.next());
        }
    }

    protected void processUnknownToken(final CommandLine cmd, final String token, final ReadAheadIterator<String> it) throws CLParserException {
        if (stopAtNonOption) {
            throw new UnknownOptionException(token);
        } else {
            cmd.addUnresolvedArg(token);
            while (it.hasNext()) {
                cmd.addUnresolvedArg(it.next());
            }
        }
    }

}
