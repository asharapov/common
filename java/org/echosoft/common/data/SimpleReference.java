package org.echosoft.common.data;

import org.echosoft.common.data.spi.SimpleReferenceJsonSerializer;
import org.echosoft.common.json.annotate.JsonUseSeriazer;

/**
 * Описывает ссылку на какой-либо бизнес-объект в системе.
 * Данный класс является простейшей реализацией интерфейса {@link Reference} в котором первичный ключ задается некоторым числом.
 *
 * @author Anton Sharapov
 */
@JsonUseSeriazer(value = SimpleReferenceJsonSerializer.class, recursive = true)
public class SimpleReference implements Reference<Long, String> {

    private final long id;
    private final String title;

    public SimpleReference(final long id) {
        this.id = id;
        this.title = Long.toString(id);
    }
    public SimpleReference(final long id, final String title) {
        this.id = id;
        this.title = title;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getDescription() {
        return title;
    }


    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !getClass().equals(obj.getClass()))
            return false;
        final SimpleReference other = (SimpleReference) obj;
        return id == other.id;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "{id:" + id + ", title:" + title + "}";
    }
}