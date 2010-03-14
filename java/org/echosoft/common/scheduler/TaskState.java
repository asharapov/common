package org.echosoft.common.scheduler;

/**
 * Состояния обработки задачи.
 * @author Anton Sharapov
 */
public enum TaskState {

    /**
     * Задача выполнена успешно.
     */
    COMPLETED,

    /**
     * Задача завершена с ошибкой.
     */
    FAILED,

    /**
     * Задача находится в процессе обработки.
     */
    PROCESSING,

    /**
     * Задача поставлена в очередь обработки.
     */
    AWAITING,

}
