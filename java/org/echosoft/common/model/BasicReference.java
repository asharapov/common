package org.echosoft.common.model;

import org.echosoft.common.utils.StringUtil;

/**
 * The simplest implementation of the {@link Reference} interface. This immutable object describes references
 * to something business object and contains minimally allowable information about him for representing reference
 * in the UI.
 *
 * @author Anton Sharapov
 */
public class BasicReference<T> implements Reference<T> {

    private final String key;
    private final String title;

    public BasicReference(final long key) {
        this(key, null);
    }

    public BasicReference(final long key, final String title) {
        this.key = Long.toString(key);
        this.title = title;
    }


    public BasicReference(final String key) {
        this(key, null);
    }

    public BasicReference(String key, final String title) {
        if ((key=StringUtil.trim(key))==null)
            throw new IllegalArgumentException("Empty key specified");
        this.key = key;
        this.title = title;
    }

    /**
     * Primary key value. Can't be <code>null</code> or empty string.
     * @return  object's primary key.
     */
    public String getKey() {
        return key;
    }

    /**
     * Primary key value as long value. Can't be <code>null</code>.
     * @return  object's primary key transformed to numeric form.
     * @exception  NumberFormatException  if the <code>key</code> does not contain a parsable <code>long</code>.
     */
    public long getKeyAsLong() {
        return Long.parseLong(key);
    }

    /**
     * Short description of the given business object, adopted for using in the UI.
     * @return  short  textual description of the object.
     */
    public String getTitle() {
        return title;
    }



    public int hashCode() {
        return key.hashCode();
    }

    public boolean equals(final Object obj) {
        if (obj==null || !getClass().equals(obj.getClass()))
            return false;
        final BasicReference other = (BasicReference)obj;
        return key.equals(other.key);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String toString() {
        return "{key:"+key+", title:"+title+"}";
    }
}
