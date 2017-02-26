package org.echosoft.common.data.misc;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.echosoft.common.data.misc.spi.VersionJsonSerializer;
import org.echosoft.common.json.annotate.JsonUseSeriazer;
import org.echosoft.common.utils.Any;
import org.echosoft.common.utils.StringUtil;

/**
 * Версия программных компонентов.
 *
 * @author Anton Sharapov
 */
@JsonUseSeriazer(value = VersionJsonSerializer.class, recursive = true)
public class Version implements Serializable, Comparable<Version> {

    private static final Pattern pattern1 = Pattern.compile("^(\\d+?)(?:\\.(\\d+)*)?(?:\\.([0-9A-Fa-f]*))?(?:[\\.-](.*))?$");
    private static final Pattern pattern2 = Pattern.compile("^([0-9A-Fa-f]+)?(?:[\\.-](.*))?$");
    private static final Pattern pattern3 = Pattern.compile("^([0-9A-Fa-f]*)$");

    /**
     * Выполняет разбор номера версии из строки.
     *
     * @param version строка с номером версии.
     * @return разобранный экземпляр {@link Version} или <code>null</code> если переданная в аргументе строка была пустой
     */
    public static Version parseVersion(String version) {
        version = StringUtil.trim(version);
        if (version == null)
            return null;

        Matcher m = pattern1.matcher(version);
        if (m.find()) {
            final String maj = m.group(1);
            final String min = m.group(2);
            final String rev = m.group(3);
            final String ext = m.group(4);
            return new Version(Any.asInt(maj,0), Any.asInt(min,0), rev, ext);
        }
        m = pattern2.matcher(version);
        if (m.find()) {
            final String rev = m.group(1);
            final String ext = m.group(2);
            return new Version(0, 0, rev, ext);
        }
        return null;
    }

    private final int major;
    private final int minor;
    private final String revision;
    private final String extraVersion;

    public Version(final int major) {
        this(major, 0, null, null);
    }

    public Version(final int major, final int minor) {
        this(major, minor, null, null);
    }

    public Version(final int major, final int minor, final int revision) {
        this(major, minor, Integer.toString(revision), null);
    }

    public Version(final int major, final int minor, final String revision) {
        this(major, minor, revision, null);
    }

    public Version(final int major, final int minor, String revision, String extraVersion) {
        if (major < 0 || minor < 0)
            throw new IllegalArgumentException("Negative arguments not allowed");
        if (revision != null) {
            revision = revision.trim();
            if (!pattern3.matcher(revision).find())
                throw new IllegalArgumentException("Illegal revision: " + revision);
        }
        if (extraVersion != null) {
            extraVersion = extraVersion.trim();
            if (extraVersion.length() > 0 && (extraVersion.charAt(0) == '-' || extraVersion.charAt(0) == '.'))
                extraVersion = extraVersion.substring(1).trim();
        }
        this.major = major;
        this.minor = minor;
        this.revision = revision != null && revision.length() > 0 && !"0".equals(revision) ? revision : null;
        this.extraVersion = extraVersion != null && extraVersion.length() > 0 ? extraVersion : null;
    }


    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public String getRevision() {
        return revision;
    }

    public String getExtraVersion() {
        return extraVersion;
    }

    @Override
    public int compareTo(final Version other) {
        if (other == null)
            return 1;
        if (major != other.major)
            return major > other.major ? 1 : -1;
        if (minor != other.minor)
            return minor > other.minor ? 1 : -1;

        int r = StringUtil.compareNullableStrings(revision, other.revision);
        if (r != 0)
            return r;
        return StringUtil.compareNullableStrings(extraVersion, other.extraVersion);
    }

    @Override
    public int hashCode() {
        return (major << 4) + minor;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !getClass().equals(obj.getClass()))
            return false;
        final Version other = (Version) obj;
        return major == other.major && minor == other.minor &&
                (revision != null ? revision.equals(other.revision) : other.revision == null) &&
                (extraVersion != null ? extraVersion.equals(other.extraVersion) : other.extraVersion == null);
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(16);
        buf.append(major).append('.').append(minor);
        if (revision != null) {
            buf.append('.').append(revision);
        }
        if (extraVersion != null) {
            if (extraVersion.charAt(0) != '-')
                buf.append('-');
            buf.append(extraVersion);
        }
        return buf.toString();
    }
}
