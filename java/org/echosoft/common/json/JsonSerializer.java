package org.echosoft.common.json;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

/**
 * Отвечает за сериализацию в JSON строку объектов определенного класса.
 *
 * @author Anton Sharapov
 */
public interface JsonSerializer<T> extends Serializable {

    /**
     * Преобразовывает заданный объект в строку JSON формата и сохраняет ее в потоке.
     *
     * @param src объект который должен быть преобразован в JSON формат.
     *            Объект должен относиться либо к классу который непосредственно ассоциирован с заданным сериализатором
     *            либо к классу-потомку класса ассоциированного с заданным сериализатором.
     * @param out выходной поток.
     * @throws IOException               в случае ошибок при помещении результата в выходной поток.
     * @throws IllegalAccessException    в случае когда вызывающий код не имеет достаточно прав для обращения к свойствам сериализуемого объекта.
     * @throws InvocationTargetException в случае если при обращении к свойствам сериализуемого объекта произошла ошибка.
     */
    public void serialize(T src, JsonWriter out) throws IOException, InvocationTargetException, IllegalAccessException;
}
