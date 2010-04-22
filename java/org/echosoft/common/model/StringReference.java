package org.echosoft.common.model;

import org.echosoft.common.json.annotate.JsonUseSeriazer;
import org.echosoft.common.model.spi.StringReferenceJsonSerializer;
import org.echosoft.common.utils.StringUtil;

/**
 * Описывает ссылку на какой-либо бизнес-объект в системе.
 * Данный класс является простейшей реализацией интерфейса {@link Reference} в котором первичный ключ
 * описывается некоторой строкой текста.
 *
 * @author Anton Sharapov
 */
@JsonUseSeriazer(value = StringReferenceJsonSerializer.class, recursive = true)
public class StringReference<T> implements Reference<T> {

    private final String id;
    private final String title;

    public StringReference(final String id) {
        final String _id = StringUtil.trim(id);
        if (_id==null)
            throw new IllegalArgumentException("Empty identificator not allowed");
        this.id = _id;
        this.title = _id;
    }
    public StringReference(final String id, final String title) {
        final String _id = StringUtil.trim(id);
        if (_id==null)
            throw new IllegalArgumentException("Empty identificator not allowed");
        this.id = _id;
        this.title = title;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getTitle() {
        return title;
    }


    @Override
    public int hashCode() {
        return id.hashCode();
    }
    @Override
    public boolean equals(final Object obj) {
        if (obj==null || !getClass().equals(obj.getClass()))
            return false;
        final StringReference other = (StringReference)obj;
        return id.equals(other.id);
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
