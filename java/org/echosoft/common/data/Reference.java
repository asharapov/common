package org.echosoft.common.data;

import java.io.Serializable;

/**
 * Описывает ссылку на какой-либо бизнес-объект в системе.
 *
 * @author Anton Sharapov
 */
public interface Reference<K extends Serializable, T extends Serializable> extends Serializable {

    /**
     * Первичный ключ объекта. Не может быть <code>null</code>.
     *
     * @return первичный ключ для данного объекта.
     */
    public K getId();

    /**
     * Краткое описание данного бизнес-объекта (как правило - адаптированное для отображения сведений об объекте пользователю в UI).
     *
     * @return краткое описание данного объекта.
     */
    public T getDescription();
}
