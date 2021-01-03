package org.echosoft.common.cli.parser;

/**
 * @author Anton Sharapov
 */
public class MissingOptionException extends CLParserException {

    private final String option;

    public MissingOptionException(final Option option) {
        this(option.getFullName()!=null ? option.getFullName() : String.valueOf(option.getShortName()));
    }

    public MissingOptionException(final String option) {
        super("Missing mandatory option: "+option);
        this.option = option;
    }

    public String getOption() {
        return option;
    }
}
