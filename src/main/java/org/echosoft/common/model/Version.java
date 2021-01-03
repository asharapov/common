package org.echosoft.common.model;

import java.io.Serializable;
import java.text.ParseException;

import org.echosoft.common.json.annotate.JsonUseSeriazer;
import org.echosoft.common.model.spi.VersionJsonSerializer;

/**
 * Описывает версию чего бы то ни было.
 *
 * @author Anton Sharapov
 */
@JsonUseSeriazer(value = VersionJsonSerializer.class, recursive = true)
public class Version implements Serializable, Comparable<Version> {

    /**
     * Выполняет разбор номера версии из строки.
     * @param version  строка с номером версии.
     * @return  разобранный экземпляр {@link Version} или <code>null</code> если переданная в аргументе строка была пустой
     * @throws ParseException  в случае некорректной строки переданной в аргументе.
     */
    public static Version parseVersion(String version) throws ParseException {
        if (version==null)
            return null;
        version = version.trim();
        final int length = version.length();
        if (length==0)
            return null;

        final int parts[] = {0, 0, 0};
        String extra;
        int p = 0, s = 0, e = 0;
        while (p<parts.length) {
            char c = 0;
            while (e<length && Character.isDigit(c=version.charAt(e)))  e++;
            if (e>s) {
                parts[p++] = Integer.parseInt( version.substring(s,e) );
                s = e;
                if (c=='.' && p<parts.length && s+1<length && Character.isDigit(version.charAt(s+1))) {
                    s = ++e;
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        if (p>0) {
            extra = version.substring(s, length).trim();
        } else 
            throw new ParseException("Invalid version "+version, s);
        return new Version(parts[0], parts[1], parts[2], extra);
    }

    private final int major;
    private final int minor;
    private final int revision;
    private final String extraVersion;

    public Version(final int major) {
        this(major, 0, 0, null);
    }

    public Version(final int major, final String extraVersion) {
        this(major, 0, 0, extraVersion);
    }

    public Version(final int major, final int minor) {
        this(major, minor, 0, null);
    }

    public Version(final int major, final int minor, final String extraVersion) {
        this(major, minor, 0, extraVersion);
    }

    public Version(final int major, final int minor, final int revision) {
        this(major, minor, revision, null);
    }

    public Version(final int major, final int minor, final int revision, final String extraVersion) {
        if (major<0 || minor<0 || revision<0)
            throw new IllegalArgumentException("Negative arguments not allowed");
        this.major = major;
        this.minor = minor;
        this.revision = revision;

        if (extraVersion!=null) {
            final String ev = extraVersion.trim();
            this.extraVersion = ev.length()>0 ? ev : null;
        } else {
            this.extraVersion = null;
        }
    }


    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getRevision() {
        return revision;
    }

    public String getExtraVersion() {
        return extraVersion;
    }

    @Override
    public int compareTo(final Version other) {
        if (other==null)
            return 1;
        if (major!=other.major)
            return major>other.major ? 1 : -1;
        if (minor!=other.minor)
            return minor>other.minor ? 1 : -1;
        if (revision!=other.revision)
            return revision>other.revision ? 1 : -1;

        if (extraVersion!=null) {
            return other.extraVersion!=null ? extraVersion.compareTo(other.extraVersion) : -1;
        } else {
            return other.extraVersion!=null ? -1 : 0;
        }
    }

    @Override
    public int hashCode() {
        return (major << 4) + minor;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj==null || !getClass().equals(obj.getClass()))
            return false;
        final Version other = (Version)obj;
        return major==other.major && minor==other.minor && revision==other.revision &&
               (extraVersion!=null ? extraVersion.equals(other.extraVersion) : other.extraVersion==null);
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(16);
        buf.append(major);
        buf.append('.');
        buf.append(minor);
        buf.append('.');
        buf.append(revision);
        if (extraVersion!=null)
            buf.append(extraVersion);
        return buf.toString();
    }
}
