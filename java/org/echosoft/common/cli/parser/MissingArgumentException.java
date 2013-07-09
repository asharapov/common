package org.echosoft.common.cli.parser;

/**
 * @author Anton Sharapov
 */
public class MissingArgumentException extends CLParserException {

    private final String option;

    public MissingArgumentException(final Option option) {
        this(option.getFullName() != null ? option.getFullName() : String.valueOf(option.getShortName()));
    }

    public MissingArgumentException(final char option) {
        this(String.valueOf(option));
    }

    public MissingArgumentException(final String option) {
        super("Missing mandatory argument for option: " + option);
        this.option = option;
    }

    public String getOption() {
        return option;
    }
}
