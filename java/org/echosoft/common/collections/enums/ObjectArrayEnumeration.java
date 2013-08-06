package org.echosoft.common.collections.enums;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * Дает возможность представления массива объектов в виде перечислимого списка, реализующего интерфейс {@link Enumeration}.
 *
 * @author Anton Sharapov
 */
public class ObjectArrayEnumeration<T> implements Enumeration<T> {

    private final T[] array;
    private final int length;
    private int index;

    /**
     * Создает новый экземпляр {@link ObjectArrayEnumeration} для итерирования по всем элементам массива переданного в аргументе метода.
     *
     * @param array массив объектов или примитивов для которых требуется создать итератор.
     * @throws NullPointerException если <code>array</code> равен <code>null</code>.
     */
    public ObjectArrayEnumeration(final T... array) {
        this.array = array;
        this.length = array.length;
        this.index = 0;
    }

    @Override
    public boolean hasMoreElements() {
        return index < length;
    }

    @Override
    public T nextElement() {
        if (index >= length)
            throw new NoSuchElementException();
        return array[index++];
    }
}
