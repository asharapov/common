package org.echosoft.common.dbf;

import java.io.Serializable;
import java.nio.charset.Charset;

/**
 * @author Anton Sharapov
 */
class FieldDescriptor implements Serializable {

    private final String name;
    private final FieldType type;
    private final int size;
    private final int precision;
    private final boolean indexed;
    private final int fieldOffset;
    private final String charsetName;
    private transient Charset charset;

    public FieldDescriptor(final byte[] buf, final int offset, final Charset charset, final int fieldOffset) throws DBFException {
        if (buf.length - offset < 32)
            throw new IllegalArgumentException("Invalid buffer size");
        this.name = Util.readZeroBasedString(buf, offset, 11, charset);
        this.type = FieldType.findByCode((char) buf[offset + 11]);
        if (type == null)
            throw new DBFException("Unknown DBF field type: '" + (char) buf[offset + 11]);
        switch (type) {
            case LOGICAL: {
                size = 1;
                precision = 0;
                break;
            }
            case DATE: {
                size = 8;
                precision = 0;
                break;
            }
            case NUMERIC:
            case FLOAT: {
                size = buf[offset + 16];
                precision = buf[offset + 17];
                if (size > 20 || precision > size)
                    throw new DBFException("Unsupported field size");
                break;
            }
            case MEMO:
            case VARIABLE:
            case PICTURE:
            case BINARY:
            case GENERAL: {
                size = 10;
                precision = 0;
                break;
            }
            default: {
                this.size = buf[offset + 16];
                this.precision = buf[offset + 17];
            }
        }
        this.indexed = buf[offset + 31] == 1;
        this.fieldOffset = fieldOffset;
        this.charsetName = charset.name();
        this.charset = charset;
    }

    public String getName() {
        return name;
    }

    public FieldType getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public int getPrecision() {
        return precision;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public int getFieldOffset() {
        return fieldOffset;
    }

    public Charset getCharset() {
        if (charset == null)
            charset = Charset.forName(charsetName);
        return charset;
    }

    @Override
    public String toString() {
        return "[DBF.FieldDesc{name:" + name + ", type:" + type + ", size:" + size + ", precision:" + precision + ", indexed:" + indexed + ", offset:" + fieldOffset + "}]";
    }
}
