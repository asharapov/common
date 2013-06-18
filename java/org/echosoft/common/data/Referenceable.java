package org.echosoft.common.data;

import java.io.Serializable;

/**
 * @author Anton Sharapov
 */
public interface Referenceable<K extends Serializable, T extends Serializable> {

    public Reference<K, T> getReference();
}
