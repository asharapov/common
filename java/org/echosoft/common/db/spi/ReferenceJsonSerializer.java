package org.echosoft.common.db.spi;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;

import org.echosoft.common.db.Reference;
import org.echosoft.common.json.JsonSerializer;
import org.echosoft.common.json.JsonUtil;
import org.echosoft.common.json.JsonWriter;

/**
 * Сериализует в JSON формат экземпляры классов, реализующие интерфейс {@link Reference}.
 *
 * @author Anton Sharapov
 */
public class ReferenceJsonSerializer implements JsonSerializer<Reference> {

    @Override
    public void serialize(final Reference src, final JsonWriter jw) throws IOException, InvocationTargetException, IllegalAccessException {
        final Writer out = jw.getOutputWriter();
        out.write("{id:");
        out.write(src.getId().toString());
        out.write(",title:");
        JsonUtil.encodeString(src.getTitle(), out);
        out.write('}');
    }
}