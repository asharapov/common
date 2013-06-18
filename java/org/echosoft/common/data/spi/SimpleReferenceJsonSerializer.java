package org.echosoft.common.data.spi;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;

import org.echosoft.common.data.Reference;
import org.echosoft.common.json.JsonSerializer;
import org.echosoft.common.json.JsonUtil;
import org.echosoft.common.json.JsonWriter;

/**
 * Сериализует в JSON формат экземпляры классов, реализующие интерфейс {@link Reference}.
 *
 * @author Anton Sharapov
 */
public class SimpleReferenceJsonSerializer implements JsonSerializer<Reference<Number, String>> {

    @Override
    public void serialize(final Reference<Number, String> src, final JsonWriter jw) throws IOException, InvocationTargetException, IllegalAccessException {
        final Writer out = jw.getOutputWriter();
        out.write("{id:");
        out.write(src.getId().toString());
        out.write(",desc:");
        JsonUtil.encodeString(src.getDescription(), out);
        out.write('}');
    }
}