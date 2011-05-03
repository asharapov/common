package org.echosoft.common.cli.parser;

import java.io.Serializable;

import org.echosoft.common.utils.StringUtil;

/**
 * Содержит описание одной опции в командной строки.
 * @author Anton Sharapov
 */
public class Option implements Serializable, Cloneable {

    private static final String DEFAULT_ARG_NAME = "arg";

    private final Character shortName;
    private final String fullName;
    private boolean required;
    private boolean hasArgs;
    private boolean argsRequired;
    private String argName;
    private String description;

    public Option(final Character shortName, final String fullName, final boolean hasArgs, final String description) {
        this(shortName, fullName, false, hasArgs, hasArgs, description);
    }

    public Option(final Character shortName, final String fullName, boolean required, boolean hasArgs, boolean requiredArgs, String description) throws IllegalArgumentException {
        this.shortName = shortName;
        this.fullName = StringUtil.trim(fullName);
        this.required = required;
        this.hasArgs = hasArgs;
        this.required = requiredArgs;
        this.argName = DEFAULT_ARG_NAME;
        this.description = StringUtil.trim(description);
        if (this.shortName==null && this.fullName==null)
            throw new IllegalArgumentException("Option names not specified");
    }

    /**
     * Возвращает краткое (однобуквенное) название опции или <code>null</code>.
     * @return  краткое название опции или <code>null</code>.
     */
    public Character getShortName() {
        return shortName;
    }

    /**
     * Возвращает полное название опции или <code>null</code>.
     * @return Возвращает полное название опции или <code>null</code>.
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Возвращает <code>true</code> если опция должна быть обязательно указана в командной строке.
     * @return <code>true</code> если опция должна быть обязательно указана в командной строке.
     */
    public boolean isRequired() {
        return required;
    }
    public Option setRequired(final boolean required) {
        this.required = required;
        return this;
    }

    /**
     * Возвращает <code>true</code> если опция <u>может</u> иметь параметр.<br/>
     * Определение того является ли данный параметр обязательным следует из свойства {@link #isArgsRequired()}.<br/>
     * Установка данного свойства в <code>false</code> автоматически влечет за собой и установку свойства {@link #isArgsRequired()} в <code>false</code>.
     * @return  <code>true</code> если опция <u>может</u> иметь параметр.
     */
    public boolean hasArgs() {
        return hasArgs;
    }
    public Option setArgs(final boolean hasArgs) {
        this.hasArgs = hasArgs;
        if (!hasArgs)
            argsRequired = false;
        return this;
    }

    /**
     * Возвращает <code>true</code> если данная опция <u>требует</u> наличия дополнительного параметра.<br/>
     * Установка данного свойства в <code>true</code> автоматически влечет за собой и установку свойства {@link #hasArgs()}.
     * @return <code>true</code> если данная опция <u>требует</u> наличия дополнительного параметра.
     */
    public boolean isArgsRequired() {
        return argsRequired;
    }
    public Option setArgsRequired(final boolean requiresArgs) {
        this.argsRequired = requiresArgs;
        if (argsRequired)
            hasArgs = true;
        return this;
    }

    /**
     * Возвращает название аргумента опции. Используется только при печати строки с подсказкой по опциям допустимым в разбираемой командной строке.
     * По умолчанию возвращает {@link #DEFAULT_ARG_NAME}.
     * @return название аргумента опции используемое при печати подсказки по опциям командной строки
     */
    public String getArgName() {
        return argName;
    }
    public Option setArgName(final String argName) {
        this.argName = argName!=null ?argName.trim() : DEFAULT_ARG_NAME;
        return this;
    }

    /**
     * Возвращает описание опции. Используеся при печати подсказки по опциям допустимым в разбираемой командной строке.
     * @return  описание опции.
     */
    public String getDescription() {
        return description;
    }
    public Option setDescription(final String description) {
        this.description = StringUtil.trim(description);
        return this;
    }

    @Override
    public int hashCode() {
        return fullName!=null ? fullName.hashCode() : shortName.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj==null || !getClass().equals(obj.getClass()))
            return false;
        final Option other = (Option)obj;
        return (shortName!=null ? shortName.equals(other.shortName) : other.shortName==null) &&
               (fullName!=null ? fullName.equals(other.fullName) : other.fullName==null);
//               required==other.required && hasArgs==other.hasArgs && argsRequired==other.argsRequired &&
//               (argName!=null ? argName.equals(other.argName) : other.argName==null) &&
//               (description!=null ? description.equals(other.description) : other.description==null);
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(64);
        buf.append("[Option{full:").append(fullName).append(", alias:").append(shortName).append(", required:").append(required).append(", arg:");
        if (hasArgs) {
            buf.append(argsRequired ? "required" : "optional");
        } else {
            buf.append("-");
        }
        buf.append(", description:").append(description);
        buf.append("}]");
        return buf.toString();
    }
}
