package org.echosoft.common.cli.parser;

import org.echosoft.common.utils.DateUtil;
import org.echosoft.common.utils.StringUtil;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Содержит результат разбора аргументов командной строки.
 *
 * @author Anton Sharapov
 */
public class CommandLine implements Serializable {

    /**
     * @return Список поддерживаемых форматов даты и времени.
     */
    public static List<String> getSupportedDatePatterns() {
        return Collections.unmodifiableList(Arrays.asList(DEFAULT_DATE_PATTERNS));
    }

    private static final String[] DEFAULT_DATE_PATTERNS = {"dd.MM.yyyy'T'HH:mm", "dd.MM.yyyy HH:mm", "dd.MM.yyyy", "yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd HH:mm", "yyyy-MM-dd"};

    private final Options options;
    private final List<String> unresolvedArgs;
    private final Map<Option, String> values;
    private final Collection<String> datePatterns;
    private boolean extendedDateFormatAllowed;

    CommandLine(final Options options) {
        this.options = options;
        this.unresolvedArgs = new ArrayList<String>();
        this.values = new HashMap<Option, String>();
        this.datePatterns = new ArrayList<String>(Arrays.asList(DEFAULT_DATE_PATTERNS));
        this.extendedDateFormatAllowed = false;
    }

    /**
     * Если <code>true</code> то сигнализирует о наличии в командной строке нераспознанных в процессе парсинга токенов.
     *
     * @return <code>true</code> при наличии в командной строке нераспознанных токенов.
     */
    public boolean hasUnresolvedArgs() {
        return unresolvedArgs.size() > 0;
    }

    /**
     * Возвращает неразобранную часть аргументов командной строки. Если таковая отсутствует, то метод вернет пустой список.
     *
     * @return часть аргументов командной строки оставшихся неразобранными.
     */
    public String[] getUnresolvedArgs() {
        return unresolvedArgs.toArray(new String[unresolvedArgs.size()]);
    }

    /**
     * Возвращает множество всех опций которые были указаны в аргументах командной строки.
     *
     * @return множество опций которые были указаны в аргументах командной строки.
     */
    public Set<Option> getOptions() {
        return values.keySet();
    }

    /**
     * Определяет была ли указана в аргументах командной строки указанная опция.
     *
     * @param option опция.
     * @return <code>true</code> если указанная опция была указана в аргументах командной строки.
     * @throws UnknownOptionException поднимается в случае когда указанная в аргументе опция не была предварительно задекларирована в списке допустимых,
     *                                т.е. данная опция отсутствовала в списке опций, переданных парсеру командной строки.
     */
    public boolean hasOption(final Option option) throws CLParserException {
        if (!options.hasOption(option))
            throw new UnknownOptionException(option);
        return values.keySet().contains(option);
    }

    /**
     * Возвращает <code>true</code> если опция с указанным именем присутствует в командной строке.
     *
     * @param optionName краткое либо полное название опции.
     * @return <code>true</code> если указанная опция присутствует в командной строке.
     * @throws UnknownOptionException поднимается в случае когда указанная в аргументе опция не была предварительно задекларирована в списке допустимых,
     *                                т.е. данная опция отсутствовала в списке опций, переданных парсеру командной строки.
     */
    public boolean hasOption(final String optionName) throws CLParserException {
        final Option opt = options.getOption(optionName);
        if (opt == null)
            throw new UnknownOptionException(optionName);
        return values.containsKey(opt);
    }

    /**
     * Возвращает значение опции если она присутствует в разобранной командной строке.
     *
     * @param option       опция чье значение требуется возвратить.
     * @param defaultValue значение по умолчанию, возвращается данным методом если указанная опция отсутствовала в разобранной командной строке.
     * @return значение указанной опции в командной строке либо значение по умолчанию если указанная опция в командной строке не присутствовала.
     * @throws UnknownOptionException поднимается в случае когда указанная в аргументе опция не была предварительно задекларирована в списке допустимых,
     *                                т.е. данная опция отсутствовала в списке опций, переданных парсеру командной строки.
     */
    public String getOptionValue(final Option option, final String defaultValue) throws CLParserException {
        if (!options.hasOption(option))
            throw new UnknownOptionException(option);
        final String result = values.get(option);
        return result != null ? result : defaultValue;
    }

    /**
     * Возвращает значение опции если она присутствует в разобранной командной строке.
     *
     * @param optionName   краткое либо полное название опции чье значение в командной строке требуется возвратить.
     * @param defaultValue значение по умолчанию, возвращается данным методом если указанная опция отсутствовала в разобранной командной строке.
     * @return значение указанной опции в командной строке либо значение по умолчанию если указанная опция в командной строке не присутствовала.
     * @throws UnknownOptionException поднимается в случае когда указанная в аргументе опция не была предварительно задекларирована в списке допустимых,
     *                                т.е. данная опция отсутствовала в списке опций, переданных парсеру командной строки.
     */
    public String getOptionValue(final String optionName, final String defaultValue) throws CLParserException {
        final Option opt = options.getOption(optionName);
        if (opt == null)
            throw new UnknownOptionException(optionName);
        final String result = values.get(opt);
        return result != null ? result : defaultValue;
    }

    /**
     * Возвращает значение опции в виде целого числа.
     *
     * @param optionName   краткое либо полное название опции чье значение в командной строке требуется возвратить.
     * @param defaultValue значение по умолчанию, возвращается данным методом если указанная опция отсутствовала в разобранной командной строке.
     * @return значение указанной опции в командной строке либо значение по умолчанию если указанная опция в командной строке не присутствовала.
     * @throws UnknownOptionException поднимается в случае когда указанная в аргументе опция не была предварительно задекларирована в списке допустимых,
     *                                т.е. данная опция отсутствовала в списке опций, переданных парсеру командной строки.
     * @throws CLParserException      поднимается в случае ошибок конвертации строки со значением опции в целое число.
     */
    public Integer getOptionIntValue(final String optionName, final Integer defaultValue) throws CLParserException {
        final Option opt = options.getOption(optionName);
        if (opt == null)
            throw new UnknownOptionException(optionName);
        final String result = values.get(opt);
        if (result != null) {
            try {
                return Integer.parseInt(result);
            } catch (NumberFormatException e) {
                throw new CLParserException(e.getMessage(), e);
            }
        } else
            return defaultValue;
    }

    /**
     * Возвращает значение опции в виде большого целого числа.
     *
     * @param optionName   краткое либо полное название опции чье значение в командной строке требуется возвратить.
     * @param defaultValue значение по умолчанию, возвращается данным методом если указанная опция отсутствовала в разобранной командной строке.
     * @return значение указанной опции в командной строке либо значение по умолчанию если указанная опция в командной строке не присутствовала.
     * @throws UnknownOptionException поднимается в случае когда указанная в аргументе опция не была предварительно задекларирована в списке допустимых,
     *                                т.е. данная опция отсутствовала в списке опций, переданных парсеру командной строки.
     * @throws CLParserException      поднимается в случае ошибок конвертации строки со значением опции в целое число.
     */
    public Long getOptionLongValue(final String optionName, final Long defaultValue) throws CLParserException {
        final Option opt = options.getOption(optionName);
        if (opt == null)
            throw new UnknownOptionException(optionName);
        final String result = values.get(opt);
        if (result != null) {
            try {
                return Long.parseLong(result);
            } catch (NumberFormatException e) {
                throw new CLParserException(e.getMessage(), e);
            }
        } else
            return defaultValue;
    }

    /**
     * Возвращает значение опции в виде числа с плавающей запятой.
     *
     * @param optionName   краткое либо полное название опции чье значение в командной строке требуется возвратить.
     * @param defaultValue значение по умолчанию, возвращается данным методом если указанная опция отсутствовала в разобранной командной строке.
     * @return значение указанной опции в командной строке либо значение по умолчанию если указанная опция в командной строке не присутствовала.
     * @throws UnknownOptionException поднимается в случае когда указанная в аргументе опция не была предварительно задекларирована в списке допустимых,
     *                                т.е. данная опция отсутствовала в списке опций, переданных парсеру командной строки.
     * @throws CLParserException      поднимается в случае ошибок конвертации строки со значением опции в число.
     */
    public Double getOptionDoubleValue(final String optionName, final Double defaultValue) throws CLParserException {
        final Option opt = options.getOption(optionName);
        if (opt == null)
            throw new UnknownOptionException(optionName);
        final String result = values.get(opt);
        if (result != null) {
            try {
                return Double.parseDouble(result);
            } catch (NumberFormatException e) {
                throw new CLParserException(e.getMessage(), e);
            }
        } else
            return defaultValue;
    }

    /**
     * Возвращает значение опции в виде даты определенного формата.
     *
     * @param optionName   краткое либо полное название опции чье значение в командной строке требуется возвратить.
     * @param defaultValue значение по умолчанию, возвращается данным методом если указанная опция отсутствовала в разобранной командной строке.
     * @return значение указанной опции в командной строке либо значение по умолчанию если указанная опция в командной строке не присутствовала.
     * @throws UnknownOptionException поднимается в случае когда указанная в аргументе опция не была предварительно задекларирована в списке допустимых,
     *                                т.е. данная опция отсутствовала в списке опций, переданных парсеру командной строки.
     * @throws CLParserException      поднимается в случае ошибок конвертации строки в дату указанного формата
     */
    public Date getOptionDateValue(final String optionName, final Date defaultValue) throws CLParserException {
        final Option opt = options.getOption(optionName);
        if (opt == null)
            throw new UnknownOptionException(optionName);
        final String textValue = values.get(opt);
        if (textValue != null) {
            try {
                Throwable lastCause = null;
                for (String pattern : datePatterns) {
                    final SimpleDateFormat formatter = new SimpleDateFormat(pattern);
                    try {
                        return formatter.parse(textValue);
                    } catch (ParseException e) {
                        lastCause = e;
                    }
                }
                if (extendedDateFormatAllowed) {
                    try {
                        final Date result = DateUtil.calculate(new Date(), textValue);
                        if (result != null)
                            return result;
                    } catch (ParseException e) {
                        lastCause = e;
                    }
                }
                if (lastCause != null)
                    throw new CLParserException(lastCause.getMessage(), lastCause);
            } catch (CLParserException e) {
                throw e;
            } catch (Exception e) {
                throw new CLParserException(e.getMessage(), e);
            }
        }
        return defaultValue;
    }

    public Collection<String> getDatePatterns() {
        return datePatterns;
    }
    public void setDatePatterns(final String... patterns) {
        datePatterns.clear();
        for (String pattern : patterns) {
            pattern = StringUtil.trim(pattern);
            if (pattern != null)
                datePatterns.add(pattern);
        }
        if (datePatterns.isEmpty())
            datePatterns.addAll(Arrays.asList(DEFAULT_DATE_PATTERNS));
    }

    public boolean isExtendedDateFormatAllowed() {
        return extendedDateFormatAllowed;
    }
    public void setExtendedDateFormatAllowed(final boolean extendedDateFormatAllowed) {
        this.extendedDateFormatAllowed = extendedDateFormatAllowed;
    }

    void addUnresolvedArg(final String token) {
        if (token != null && !token.isEmpty())
            unresolvedArgs.add(token);
    }

    void setOptionValue(final Option option, final String value) throws CLParserException {
        if (!options.hasOption(option))
            throw new UnknownOptionException(option);
        values.put(option, value);
    }

    void setOption(final Option option) throws CLParserException {
        setOptionValue(option, null);
    }

}
