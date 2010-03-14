package org.echosoft.common.scheduler;

/**
 * Приоритет выполнения задачи.
 * @author Anton Sharapov
 */
public enum TaskPriority {

    /**
     * Наивысший приоритет.
     */
    HIGHEST,

    /**
     * Высокий приоритет.
     */
    HIGH,

    /**
     * Средний приоритет (рекомендуемое по умолчанию значение)
     */
    MEDIUM,

    /**
     * Низкий приоритет.
     */
    LOW,

    /**
     * Низший приоритет.
     */
    LOWEST

}
