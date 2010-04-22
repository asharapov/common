package org.echosoft.common.model;

import org.echosoft.common.json.annotate.JsonUseSeriazer;
import org.echosoft.common.model.spi.LongReferenceJsonSerializer;

/**
 * Описывает ссылку на какой-либо бизнес-объект в системе.
 * Данный класс является простейшей реализацией интерфейса {@link Reference} в котором первичный ключ
 * описывается некоторой строкой текста.
 *
 * @author Anton Sharapov
 */
@JsonUseSeriazer(value = LongReferenceJsonSerializer.class, recursive = true)
public class LongReference<T> implements Reference<T> {

    private final long id;
    private final String title;

    public LongReference(final long id) {
        this.id = id;
        this.title = Long.toString(id);
    }
    public LongReference(final long id, final String title) {
        this.id = id;
        this.title = title;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }


    @Override
    public int hashCode() {
        return (int)id;
    }
    @Override
    public boolean equals(final Object obj) {
        if (obj==null || !getClass().equals(obj.getClass()))
            return false;
        final LongReference other = (LongReference)obj;
        return id==other.id;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    @Override
    public String toString() {
        return "{key:"+ id +", title:"+title+"}";
    }
}