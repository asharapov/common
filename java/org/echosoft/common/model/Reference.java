package org.echosoft.common.model;

import java.io.Serializable;

/**
 * Describes references to something system object. Contains minimally allowable information about business
 * object for representing reference to him in the UI.
 *
 * @author Anton Sharapov
 */
public interface Reference<T> extends Serializable, Cloneable {

    /**
     * Primary key value. Can't be <code>null</code> or empty string.
     * @return  object's primary key.
     */
    public String getKey();

    /**
     * Primary key value as long value. Can't be <code>null</code>.
     * @return  object's primary key transformed to numeric form.
     * @exception  NumberFormatException  if the <code>key</code> does not contain a parsable <code>long</code>.
     */
    public long getKeyAsLong();

    /**
     * Short description of the given business object, adopted for using in the UI.
     * @return  short  textual description of the object.
     */
    public String getTitle();

}
