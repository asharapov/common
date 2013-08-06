package org.echosoft.common.collections;

/**
 * Содержит ряд общеупотребительных предикатов.
 *
 * @author Anton Sharapov
 */
public class Predicates {

    /**
     * Предикат, возвращающий <code>true</code> для абсолютно всех входных данных.
     */
    public static final Predicate ALL =
            new Predicate() {
                public boolean accept(final Object input) {
                    return true;
                }
            };

    /**
     * Предикат, возвращающий <code>false</code> для абсолютно всех входных данных.
     */
    public static final Predicate NOTHING =
            new Predicate() {
                public boolean accept(final Object input) {
                    return false;
                }
            };


    /**
     * Метод возвращает параметризированный предикат, возвращающий <code>true</code> для абсолютно всех входных данных.
     *
     * @return параметризированный предикат, возвращающий <code>true</code> для абсолютно всех входных данных.
     */
    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> all() {
        return (Predicate<T>) ALL;
    }

    /**
     * Метод возвращает параметризированный предикат, возвращающий <code>true</code> для абсолютно всех входных данных.
     *
     * @return параметризированный предикат, возвращающий <code>false</code> для абсолютно всех входных данных.
     */
    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> nothing() {
        return (Predicate<T>) NOTHING;
    }
}
