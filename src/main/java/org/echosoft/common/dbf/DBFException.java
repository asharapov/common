package org.echosoft.common.dbf;

/**
 * Поднимается в случае каких-либо проблем в структуре DBF файла.
 *
 * @author Anton Sharapov
 */
public class DBFException extends Exception {

    private int position;

    public DBFException(final String message) {
        super(message);
    }

    public DBFException(final Throwable cause) {
        super(cause);
    }

    public DBFException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public DBFException(final String message, final int position) {
        super(message);
        this.position = position;
    }

    public DBFException(final String message, final Throwable cause, final int position) {
        super(message, cause);
        this.position = position;
    }

    /**
     * В тех случаях где это возможно возвращает позицию в файле где была обнаружена ошибка.
     *
     * @return позиция в файле где была обнаружена ошибка или <code>0</code>.
     */
    public int getPosition() {
        return position;
    }
}
