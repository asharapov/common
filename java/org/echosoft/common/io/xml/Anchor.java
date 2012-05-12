package org.echosoft.common.io.xml;

import org.echosoft.common.collections.ObjectArrayIterator;
import org.echosoft.common.utils.StringUtil;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Определяет местоложение определенного тега в документе XML.
 *
 * @author Anton Sharapov
 */
public class Anchor implements Serializable {

    private static final Part[] EMPTY_PARTS = new Part[0];
    private static final Anchor EMPTY_ANCHOR = new Anchor();

    public static Anchor parseString(final String text) {
        final String[] tokens = StringUtil.splitIgnoringEmpty(text, '/');
        if (tokens == null || tokens.length == 0)
            return EMPTY_ANCHOR;
        final Part[] parts = new Part[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            final String token = tokens[i];
            final int p = token.indexOf(':');
            final String localPrefix = p > 0 ? token.substring(0, p) : "";
            final int lb = token.indexOf('[', p + 1);
            final String name;
            int index = 1;
            if (lb > 0) {
                name = token.substring(p + 1, lb);
                final int rb = token.indexOf(']', lb);
                if (rb > 0) {
                    final String idx = token.substring(lb + 1, rb);
                    index = Integer.parseInt(idx.trim());
                } else
                    throw new IllegalArgumentException("Invalid anchor format: " + text);
            } else {
                name = token.substring(p + 1);
            }
            final QName qn = new QName(null, name.trim(), localPrefix.trim());
            parts[i] = new Part(qn, index);
        }
        return new Anchor(parts);
    }

    private final Part[] parts;
    private transient int hash;

    public Anchor() {
        this.parts = EMPTY_PARTS;
    }

    public Anchor(final Part... parts) {
        this.parts = parts == null || parts.length == 0 ? EMPTY_PARTS : parts;
    }

    public Anchor(final List<Part> parts) {
        this.parts = parts == null || parts.isEmpty() ? EMPTY_PARTS : parts.toArray(new Part[parts.size()]);
    }

    public boolean isRoot() {
        return parts.length == 0;
    }

    public int getSize() {
        return parts.length;
    }

    public Part getPart(final int index) {
        if (index < 0 || index >= parts.length)
            throw new ArrayIndexOutOfBoundsException(index);
        return parts[index];
    }

    public Iterable<Part> getParts() {
        return new Iterable<Part>() {
            @Override
            public Iterator<Part> iterator() {
                return new ObjectArrayIterator<Part>(parts);
            }
        };
    }

    public Part[] toArray(final Part[] a) {
        if (a.length < parts.length) {
            final Part[] result = new Part[parts.length];
            System.arraycopy(parts, 0, result, 0, parts.length);
            return result;
        }
        System.arraycopy(parts, 0, a, 0, parts.length);
        if (a.length > parts.length)
            a[parts.length] = null;
        return a;
    }

    public Anchor getParent() {
        switch (parts.length) {
            case 0:
                return null;
            case 1:
                return EMPTY_ANCHOR;
            default: {
                final Part[] array = new Part[parts.length - 1];
                System.arraycopy(parts, 0, array, 0, parts.length - 1);
                return new Anchor(array);
            }
        }
    }

    public Anchor getChild(final QName name, final int index) {
        final Part[] array = new Part[parts.length + 1];
        System.arraycopy(parts, 0, array, 0, parts.length);
        array[parts.length] = new Part(name, index);
        return new Anchor(array);
    }

    public Anchor getChild(final String name, final int index) {
        final Part[] array = new Part[parts.length + 1];
        System.arraycopy(parts, 0, array, 0, parts.length);
        array[parts.length] = new Part(name, index);
        return new Anchor(array);
    }

    @Override
    public int hashCode() {
        if (hash == 0)
            hash = Arrays.hashCode(parts);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !getClass().equals(obj.getClass()))
            return false;
        final Anchor other = (Anchor) obj;
        if (parts.length != other.parts.length)
            return false;
        for (int i = parts.length - 1; i >= 0; i--) {
            if (!parts[i].equals(other.parts[i]))
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (parts.length == 0)
            return "/";
        final StringBuilder buf = new StringBuilder();
        for (Part part : parts) {
            buf.append('/');
            if (!part.name.getPrefix().isEmpty())
                buf.append(part.name.getPrefix()).append(':');
            buf.append(part.name.getLocalPart()).append('[').append(part.index).append(']');
        }
        return buf.toString();
    }


    public static class Part implements Serializable {
        private final QName name;
        private final int index;

        public Part(final QName name, final int index) {
            if (name == null || index <= 0)
                throw new IllegalArgumentException();
            this.name = name;
            this.index = index;
        }

        public Part(final String name, final int index) {
            this(new QName(name), index);
        }

        public QName getName() {
            return name;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public int hashCode() {
            return name.hashCode() + index;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null || !getClass().equals(obj.getClass()))
                return false;
            final Part other = (Part) obj;
            return (name != null ? name.equals(other.name) : other.name == null) &&
                    index == other.index;
        }

        @Override
        public String toString() {
            final StringBuilder buf = new StringBuilder();
            if (!name.getPrefix().isEmpty())
                buf.append(name.getPrefix()).append(':');
            buf.append(name.getLocalPart()).append('[').append(index).append(']');
            return buf.toString();
        }
    }

}
