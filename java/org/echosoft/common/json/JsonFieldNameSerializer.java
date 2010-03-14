package org.echosoft.common.json;

import java.io.IOException;
import java.io.Writer;

/**
 * Отвечает за сериализацию имен свойств объектов согласно соглашениям принятым в языке JavaScript.
 *
 * @author Anton Sharapov
 */
public interface JsonFieldNameSerializer {

    /**
     * Сериализует указанное имя свойства в выходной поток.
     * @param fieldName  имя свойства объекта javascript.
     * @param out  выходной поток.
     * @throws IOException  в случае когда имя свойства не может быть сериализовано в поток или
     *                      в случае каких-либо ошибок при помещении результата в поток.
     */
    public void serialize(String fieldName, Writer out) throws IOException;
}
