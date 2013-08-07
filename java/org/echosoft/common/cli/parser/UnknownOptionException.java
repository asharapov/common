package org.echosoft.common.cli.parser;

/**
 * @author Anton Sharapov
 */
public class UnknownOptionException extends CLParserException {

    private final String option;

    public UnknownOptionException(final Option option) {
        this(option.getFullName() != null ? option.getFullName() : String.valueOf(option.getShortName()));
    }

    public UnknownOptionException(final char option) {
        this(String.valueOf(option));
    }

    public UnknownOptionException(final String option) {
        super("Unknown option: " + option);
        this.option = option;
    }

    public String getOption() {
        return option;
    }
}
