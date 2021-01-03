package org.echosoft.common.model;

import java.io.Serializable;

/**
 * Описывает ссылку на какой-либо бизнес-объект в системе.
 * Содержит минимально необходимую информацию об объекте для его представления в UI.
 *
 * @author Anton Sharapov
 */
public interface Reference<T> extends Serializable, Cloneable {

    /**
     * Первичный ключ объекта. Не может быть <code>null</code>.
     * @return  первичный ключ для данного объекта.
     */
    public Serializable getId();

    /**
     * Краткое описание данного бизнес-объекта, адаптированное для использования в пользовательском интерфейсе.
     * @return  краткое описание данного объекта, позволяющее в пользовательском интерфейсе отличить его от других объектов.
     */
    public String getTitle();

}
