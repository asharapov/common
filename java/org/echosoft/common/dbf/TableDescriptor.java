package org.echosoft.common.dbf;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import org.echosoft.common.utils.StringUtil;

/**
 * @author Anton Sharapov
 */
class TableDescriptor implements Serializable {

    private final int version;
    private final Date date;
    private final int recordsCount;
    private final int headerSize;
    private final int recordSize;
    private final boolean hasIndex;
    private final LanguageDriver languageDriver;

    public TableDescriptor(final byte[] buf) {
        if (buf == null || buf.length < 32)
            throw new IllegalArgumentException("Invalid buffer size");
        this.version = buf[0];
        final Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 1900 + buf[1]);
        cal.set(Calendar.MONTH, buf[2] - 1);
        cal.set(Calendar.DAY_OF_MONTH, buf[3]);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        this.date = cal.getTime();
        this.recordsCount = Util.readInt(buf, 4);
        this.headerSize = Util.readUnsignedShort(buf, 8);
        this.recordSize = Util.readUnsignedShort(buf, 10);
        this.languageDriver = LanguageDriver.findByCode(buf[29]);
        this.hasIndex = buf[28] == 1;
    }

    public int getVersion() {
        return version;
    }

    public Date getDate() {
        return date;
    }

    public int getRecordsCount() {
        return recordsCount;
    }

    public int getHeaderSize() {
        return headerSize;
    }

    public int getRecordSize() {
        return recordSize;
    }

    public boolean hasIndex() {
        return hasIndex;
    }

    public LanguageDriver getLanguageDriver() {
        return languageDriver;
    }

    @Override
    public String toString() {
        return "[DBF.Table{ver:" + version + ", date:" + StringUtil.formatDate(date) + ", records:" + recordsCount + ", headerSize:" + headerSize + ", recordSize:" + recordSize + "}]";
    }
}
