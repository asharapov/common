package org.echosoft.common.collections;

import java.lang.reflect.Array;
import java.util.NoSuchElementException;

/**
 * Простая реализация итератора поверх массива объектов или примитивов.<br/>
 * Если заведомо известно что имеется массив объектов, то предпочтительнее использовать класс {@link ObjectArrayIterator} по соображениям производительности.
 * Данная реализация не поддерживает удаление элементов итераторов, соответственно метод {@link #remove()} всегда поднимает исключение.
 *
 * @author Anton Sharapov
 */
public class ArrayIterator implements ReadAheadIterator {

    private final Object array;
    private final int length;
    private int index;


    /**
     * Создает новый экземпляр {@link ArrayIterator} для итерирования по всем элементам массива переданного в аргументе метода.
     *
     * @param array массив объектов или примитивов для которых требуется создать итератор.
     * @throws IllegalArgumentException если <code>array</code> не является массивом.
     * @throws NullPointerException     если <code>array</code> равен <code>null</code>.
     */
    public ArrayIterator(final Object array) {
        this.array = array;
        this.length = Array.getLength(array);
        this.index = 0;
    }

    @Override
    public boolean hasNext() {
        return index < length;
    }

    @Override
    public Object next() {
        if (index >= length)
            throw new NoSuchElementException();
        return Array.get(array, index++);
    }

    /**
     * В данной реализации метод всегда бросает исключение {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException при каждом вызове метода
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove() method is not supported");
    }

    @Override
    public Object readAhead() {
        if (index >= length)
            throw new NoSuchElementException();
        return Array.get(array, index);
    }
}
