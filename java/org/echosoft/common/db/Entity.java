package org.echosoft.common.db;

import java.io.Serializable;

/**
 * @author Anton Sharapov
 */
public interface Entity<T extends Serializable> {

    public T getId();

}
