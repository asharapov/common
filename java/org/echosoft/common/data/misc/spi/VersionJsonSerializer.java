package org.echosoft.common.data.misc.spi;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;

import org.echosoft.common.json.JsonSerializer;
import org.echosoft.common.json.JsonUtil;
import org.echosoft.common.json.JsonWriter;
import org.echosoft.common.data.misc.Version;

/**
 * Сериализует в JSON формат экземпляры класса {@link Version}.
 * @author Anton Sharapov
 */
public class VersionJsonSerializer implements JsonSerializer<Version> {

    @Override
    public void serialize(final Version src, final JsonWriter jw) throws IOException, InvocationTargetException, IllegalAccessException {
        final Writer out = jw.getOutputWriter();
        out.write("{major:");
        out.write( Integer.toString(src.getMajor(),10) );
        if (src.getMinor()>0 || src.getRevision()>0) {
            out.write(",minor:");
            out.write( Integer.toString(src.getMinor(),10) );
            if (src.getRevision()>0) {
                out.write(",rev:");
                out.write( Integer.toString(src.getRevision(),10) );
            }
        }
        if (src.getExtraVersion()!=null) {
            out.write(",extra:");
            JsonUtil.encodeString(src.getExtraVersion(), out);
        }
        out.write('}');
    }

}
