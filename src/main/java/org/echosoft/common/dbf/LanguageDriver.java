package org.echosoft.common.dbf;


import java.nio.charset.Charset;

/**
 * @author Anton Sharapov
 */
public enum LanguageDriver {

    UNKNOWN(0x00, Charset.defaultCharset()),
    CP437(0x01, Charset.forName("cp437")),
    CP850(0x02, Charset.forName("cp850")),
    CP866(0x26, Charset.forName("cp866")),
    CP1251(0x57, Charset.forName("cp1251")),
    CP1250(0xC8, Charset.forName("cp1250"));

    public static LanguageDriver findByCode(final int code) {
        for (LanguageDriver driver : values()) {
            if (driver.code == code)
                return driver;
        }
        return UNKNOWN;
    }

    private final int code;
    private final Charset charset;

    private LanguageDriver(final int code, final Charset charset) {
        this.code = code;
        this.charset = charset;
    }

    public int getCode() {
        return code;
    }

    public Charset getCharset() {
        return charset;
    }
}
