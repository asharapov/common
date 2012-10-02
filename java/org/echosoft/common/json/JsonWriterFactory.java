package org.echosoft.common.json;

import java.io.Writer;

/**
 * Предназначен для создания подходящих экземпляров {@link JsonWriter}.
 *
 * @author Anton Sharapov
 */
public interface JsonWriterFactory {

    /**
     * Создает новый экземпляр {@link JsonWriter}.
     *
     * @param ctx используемый контекст сериализации.
     * @param out выходной поток куда должен помещаться результат работы созданного экземпляра {@link JsonWriter}. Не может быть <code>null</code>.
     * @return созданный экземпляр {@link JsonWriter}.
     */
    public JsonWriter makeJsonWriter(final JsonContext ctx, final Writer out);
}
