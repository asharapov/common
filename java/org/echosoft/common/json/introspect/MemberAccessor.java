package org.echosoft.common.json.introspect;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.echosoft.common.json.JsonWriter;

/**
 * Описывает правило сериализации определенного свойства для заданного класса.
 * @author Anton Sharapov
 */
public interface MemberAccessor {

    /**
     * Сериализует свойство бина в формат JSON.
     * @param bean  бин в котором должно быть сериализовано определенное свойство
     * @param jw  выходной поток.
     * @throws IOException  в случае ошибок при помещении результата в выходной поток.
     * @throws InvocationTargetException  в случае каких-либо проблем с вызовом свойств данного объекта.
     * @throws IllegalAccessException  в случае проблем с доступ к значению свойства данного объекта.
     */
    public void serialize(Object bean, JsonWriter jw) throws IOException, InvocationTargetException, IllegalAccessException;
}
