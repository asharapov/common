package org.echosoft.common.json.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Содержит свойства сериализации отдельного свойства/поля Java класса в JSON формат.
 * @author Anton Sharapov
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface JsonField {

    /**
     * Переопределяет имя свойства/поля объекта.
     * @return  новое имя или <code>""</code> (значение по умолчанию) в случае если следует использовать реальное имя свойства/поля.
     */
    String name() default "";

    /**
     * Дает возможность отметить данное свойство/поле объекта как несериализуемое в JSON формат. Дополнительно, не сериализуются в JSON
     * все поля объектов с модификатором <code>transient</code>.
     * @return <code>true</code> если поле не должно сериализоваться в JSON. По умолчанию возвращает <code>false</code>.
     */
    boolean isTransient() default false;

    /**
     * Указывает следует ли сериализовать данное свойство если его значение равно <code>null</code>.
     * @return <code>true</code> (по умолчанию) если сериализовать пустые значения надо.
     */
    boolean writeNulls() default true;

    /**
     * Определяет режим сериализации объекта - значения аннотированного свойства/поля.
     * Если <code>true</code> то значение аннотированного свойства должно отображаться не как отдельный объект а все его свойства должны отображаться в контексте родительского объекта.<br/>
     * Довольно экзотичный режим. Практически никогда не используется. 
     * @return по умолчанию возвращает <code>false</code>.
     */
    boolean dereference() default false;

}
