package org.echosoft.common.dbf;

/**
 * @author Anton Sharapov
 */
public enum FieldType {

    CHAR('C'),
    DATE('D'),
    FLOAT('F'),
    NUMERIC('N'),
    LOGICAL('L'),
    MEMO('M'),
    VARIABLE('V'),
    PICTURE('P'),
    BINARY('B'),
    GENERAL('G');

    public static FieldType findByCode(final char code) {
        for (FieldType type : values()) {
            if (type.code == code)
                return type;
        }
        return null;
    }

    private final char code;

    private FieldType(final char code) {
        this.code = code;
    }

    public char getCode() {
        return code;
    }
}
