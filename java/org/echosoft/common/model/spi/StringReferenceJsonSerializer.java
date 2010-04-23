package org.echosoft.common.model.spi;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;

import org.echosoft.common.json.JsonSerializer;
import org.echosoft.common.json.JsonUtil;
import org.echosoft.common.json.JsonWriter;
import org.echosoft.common.model.StringReference;

/**
 * Сериализует в JSON формат экземпляры классов, реализующие интерфейс {@link StringReference}.
 * @author Anton Sharapov
 */
public class StringReferenceJsonSerializer implements JsonSerializer<StringReference> {

    @Override
    public void serialize(final StringReference src, final JsonWriter jw) throws IOException, InvocationTargetException, IllegalAccessException {
        final Writer out = jw.getOutputWriter();
        out.write("{id:");
        JsonUtil.encodeString(src.getId(), out);
        out.write(",title:");
        JsonUtil.encodeString(src.getTitle(), out);
        out.write('}');
    }

}
