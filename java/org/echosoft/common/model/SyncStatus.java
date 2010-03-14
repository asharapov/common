package org.echosoft.common.model;

/**
 * Can be used to persistence control for any managed entities.
 *
 * @author Anton Sharapov
 */
public enum SyncStatus {

    /**
     * Used whether the entity state synchronized with storage.
     */
    SYNCHRONIZED,

    /**
     * Used whether the entity has ever been saved.
     */
    ADDED,

    /**
     * Used whether the entity has been modified, since it was last retrieved from storage.
     */
    MODIFIED,

    /**
     * Used whether the entity marked for removal from storage.
     */
    REMOVED
}
