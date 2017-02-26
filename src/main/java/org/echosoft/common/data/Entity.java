package org.echosoft.common.data;

import java.io.Serializable;

/**
 * @author Anton Sharapov
 */
public interface Entity<T extends Serializable> {

    public T getId();

}
