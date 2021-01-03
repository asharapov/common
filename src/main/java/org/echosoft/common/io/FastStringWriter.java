package org.echosoft.common.io;

import java.io.IOException;
import java.io.Writer;

/**
 * <p>Поток символов, который хранит все помещаемые в него символы в некотором внутреннем буфере,
 * который при необходимости расширяется до требуемых размеров.</p>
 * <p>Функционально данный класс аналогичен стандартному <code>java.io.StringWriter</code>. Основным отличием от последнего
 * является то что все методы данного класса не синхронизованы.
 * @author Anton Sharapov
 */
public final class FastStringWriter extends Writer {

    private static final int INITIAL_CAPACITY = 16;

    /**
     * Буфер в котором хранятся все символы помещенные в данный поток.
     */
    private char value[];

    /** 
     * Реальное количество символов в буфере.
     */
    private int count;


    /**
     * Создает новый пустой экземпляр потока с настройками по умолчанию.
     */
    public FastStringWriter() {
        this(INITIAL_CAPACITY);
    }

    /**
     * Создает новый поток с предустановленной емкостью внутреннего буфера.
     * @param capacity   начальная емкость буфера.
     */
    public FastStringWriter(final int capacity) {
        value = new char[capacity];
    }

    /**
     * Создает экземпляр потока символов и сразу помещает в него указанную строку.
     * По умолчанию емкость внутреннего буфера равна длине записываемой строки плюс {@link #INITIAL_CAPACITY} символов.
     * @param text  строка для записи в поток, может быть <code>null</code>.
     */
    public FastStringWriter(final String text) {
        if (text==null) {
            value = new char[INITIAL_CAPACITY + 4];
            value[0] = 'n';
            value[1] = 'u';
            value[2] = 'l';
            value[3] = 'l';
            count = 4;
        } else {
            count = text.length();
            value = new char[INITIAL_CAPACITY + count];
            text.getChars(0, count, value, 0);
        }
    }

    /**
     * Записывает в поток указанную строку символов.
     * @param text  строка для записи в поток, может быть <code>null</code>.
     */
    @Override
    public void write(final String text) {
        if (text == null) {
            ensureCapacity(4);
            value[count++] = 'n';
            value[count++] = 'u';
            value[count++] = 'l';
            value[count++] = 'l';
        } else {
            final int length = text.length();
            ensureCapacity(length);
            text.getChars(0, length, value, count);
            count += length;
        }
    }

    /**
     * Записывает в поток фрагмент указанной в аргументе строки символов.
     * @param text  строка, чей фрагмент требуется записать в поток. Может быть <code>null</code>.
     * @param offset  смещение, по которому доступен первый символ требуемого фрагмента.
     * @param length  кол-во символов для записи в поток.
     */
    @Override
    public void write(final String text, final int offset, final int length) {
        if (text == null) {
            ensureCapacity(4);
            value[count++] = 'n';
            value[count++] = 'u';
            value[count++] = 'l';
            value[count++] = 'l';
        } else {
            ensureCapacity(length);
            text.getChars(offset, offset + length, value, count);
            count += length;
        }
    }

    /**
     * Записывает в поток массив символов.
     * @param chars  массив символов для записи в поток, не может быть <code>null</code>.
     */
    @Override
    public void write(final char chars[]) {
        final int length = chars.length;
        ensureCapacity(length);
        System.arraycopy(chars, 0, value, count, length);
        count += length;
    }

    /**
     * Записывает в поток фрагмент указанного в аргументе массива символов.
     * @param chars  массив, чей фрагмент требуется записать в поток. Может быть <code>null</code>.
     * @param offset  смещение, по которому доступен первый символ требуемого фрагмента.
     * @param length  кол-во символов для записи в поток.
     */
    public void write(final char chars[], final int offset, final int length) {
        ensureCapacity(length);
        System.arraycopy(chars, offset, value, count, length);
        count += length;
    }

    /**
     * Записывает в поток отдельный символ.
     * @param c  символ для записи в поток.
     */
    @Override
    public void write(final int c) {
        ensureCapacity(1);
        value[count++] = (char)c;
    }

    /**
     * Записывает в поток отдельный символ.
     * @param c  символ для записи в поток.
     */
    public void write(final char c) {
        ensureCapacity(1);
        value[count++] = c;
    }

    /**
     * Добавляет в поток указанную последовательность символов.
     * @param cseq  описывает последовательность символов которую требуется добавить в выходной поток, может быть <code>null</code>.
     * @return  ссылку на данный поток.
     */
    @Override
    public FastStringWriter append(final CharSequence cseq) {
        if (cseq==null) {
            ensureCapacity(4);
            value[count++] = 'n';
            value[count++] = 'u';
            value[count++] = 'l';
            value[count++] = 'l';
        } else {
            final int length = cseq.length();
            ensureCapacity(length);
            cseq.toString().getChars(0, length, value, count);
            count += length;
        }
        return this;
    }

    /**
     * Добавляет в поток фрагмент указанной последовательности символов.
     * @param cseq  описывает последовательность символов чей фрагмент требуется добавить в выходной поток, может быть <code>null</code>.
     * @param start индекс первого символа фрагмента который требуется записать в поток.
     * @param end индекс символа, следующего за последним символом требуемого фрагмента.
     * @return  ссылку на данный поток.
     */
    @Override
    public FastStringWriter append(final CharSequence cseq, final int start, final int end) {
        if (cseq==null) {
            ensureCapacity(4);
            value[count++] = 'n';
            value[count++] = 'u';
            value[count++] = 'l';
            value[count++] = 'l';
        } else {
            final CharSequence cs = cseq.subSequence(start, end);
            final int length = cs.length();
            ensureCapacity(length);
            cs.toString().getChars(0, length, value, count);
            count += length;
        }
        return this;
    }

    /**
     * Добавляет в поток отдельный символ.
     * @param c  символ для записи в поток.
     * @return  ссылку на данный поток.
     */
    @Override
    public FastStringWriter append(final char c) {
        ensureCapacity(1);
        value[count++] = c;
        return this;
    }

    /**
     * Сохраняет в поток все буфера (если они есть).
     * В данной реализации метод не делает ничего.
     */
    public void flush() {
    }

    /**
     * Сохраняет в поток все буфера (если они есть) и закрывает поток.
     * В данной реализации метод не делает ничего.
     */
    public void close() {
    }



    /**
     * Переносит весь сохраненный в потоке контент в другой поток.
     * @param out выходной поток куда будет помещено все содержимое данного потока.
     * @throws IOException в случае проблем при помещении данных в выходной поток.
     */
    public void writeOut(final Writer out) throws IOException {
        out.write(value, 0, count);
    }

    /**
     * Переносит весь сохраненный в потоке контент в другой поток.
     * @param out выходной поток куда будет помещено все содержимое данного потока.
     */
    public void writeOut(final FastStringWriter out) {
        out.write(value, 0, count);
    }

    /**
     * Возвращает количество символов, помещенных в данный поток.
     * @return  количество символов в потоке на данный момент.
     */
    public int length() {
        return count;
    }

    /**
     * Копирует запрошенный фрагмент от всего содержимого потока в указанный массив символов.
     * @param srcBegin  индекс первого символа для копирования.
     * @param srcEnd индекс символа, следующего за последним копируемым символом.
     * @param dst  целевой буфер.
     * @param dstBegin смещение по которому будет проводиться запись в целевой буфер.
     */
    public void getChars(final int srcBegin, final int srcEnd, final char dst[], final int dstBegin) {
        System.arraycopy(value, srcBegin, dst, dstBegin, srcEnd - srcBegin);
    }

    /**
     * Возвращает все символы помещенные в данный поток в виде строки.
     * @return  все символы помещенные в данный поток в виде строки. 
     */
    public String toString() {
        return new String(value, 0, count);
    }


    /**
     * Метод выполняет проверку что в буфере еще есть место для размещения дополнительных N байт, где N указывается в аргументе метода.
     * Если в буфере недостаточно свободного места то он расширяется на требуемую величину.
     * @param delta  сколько байт еще требуется положить в буфер.
     */
    private void ensureCapacity(final int delta) {
        final int required = count + delta;
        if (required > value.length) {
            int capacity = (value.length + 1) * 2;
            if (capacity < 0) {
                capacity = Integer.MAX_VALUE;
            } else
            if (required > capacity) {
                capacity = required;
            }
            final char buf[] = new char[capacity];
            System.arraycopy(value, 0, buf, 0, count);
            value = buf;
        }
    }

}
