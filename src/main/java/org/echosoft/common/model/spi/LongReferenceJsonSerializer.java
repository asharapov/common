package org.echosoft.common.model.spi;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;

import org.echosoft.common.json.JsonSerializer;
import org.echosoft.common.json.JsonUtil;
import org.echosoft.common.json.JsonWriter;
import org.echosoft.common.model.LongReference;

/**
 * Сериализует в JSON формат экземпляры классов, реализующие интерфейс {@link LongReference}.
 * @author Anton Sharapov
 */
public class LongReferenceJsonSerializer implements JsonSerializer<LongReference> {

    @Override
    public void serialize(final LongReference src, final JsonWriter jw) throws IOException, InvocationTargetException, IllegalAccessException {
        final Writer out = jw.getOutputWriter();
        out.write("{id:");
        out.write(Long.toString(src.getId()) );
        out.write(",title:");
        JsonUtil.encodeString(src.getTitle(), out);
        out.write('}');
    }

}