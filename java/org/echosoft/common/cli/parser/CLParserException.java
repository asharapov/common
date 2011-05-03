package org.echosoft.common.cli.parser;

/**
 * @author Anton Sharapov
 */
public class CLParserException extends Exception {

    public CLParserException(final String message) {
        super(message);
    }

    public CLParserException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
