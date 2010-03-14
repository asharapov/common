package org.echosoft.common.json.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Данная аннотация определяет должны ли сериализоваться те свойства бинов которые имеют значение <code>null</code>.
 * По умолчанию сериализуются все свойства бинов.
 *
 * @see org.echosoft.common.json.introspect.BeanSerializer
 * @author Anton Sharapov
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface JsonWriteNulls {

    boolean value() default true;
}
