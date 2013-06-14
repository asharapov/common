package org.echosoft.common.db;

import java.io.Serializable;

/**
 * Описывает ссылку на какой-либо бизнес-объект в системе.
 *
 * @author Anton Sharapov
 */
public interface Reference<T extends Serializable> {

    /**
     * Первичный ключ объекта. Не может быть <code>null</code>.
     *
     * @return первичный ключ для данного объекта.
     */
    public T getId();

    /**
     * Краткое описание данного бизнес-объекта, адаптированное для использования в пользовательском интерфейсе.
     *
     * @return краткое описание данного объекта, позволяющее в пользовательском интерфейсе отличить его от других объектов.
     */
    public String getTitle();
}
