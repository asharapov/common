package org.echosoft.common.json.annotate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.echosoft.common.json.JsonSerializer;

/**
 * Позволяет указать класс используемый для сериализации объектов того класса в котором была указана данная аннотация.
 * <p>пример:</p>
 * <pre>
 * &#64JsonUseSerializer(SomeClassSerializer.class)
 * public class SomeClass {
 *   public final String someField;
 * }
 * </pre>
 * В вышеприведенном примере для сериализации объектов класса <code>SomeClass</code> будет использоваться экземпляр класса <code>SomeClassSerializer</code>.<br/>
 * Данная аннотация используется только тогда когда контекст еще не имеет информации о том какой сериализатор должен использоваться для сериализации объектов данного класса.
 * @author Anton Sharapov
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface JsonUseSeriazer {

    /**
     * Класс сериализера который будет ассоциирован с данным классом.
     * @return класс сериализера.
     */
    public  Class<? extends JsonSerializer> value();

    /**
     * Определяет следует ли использовать указанный сериализер для унаследованных классов.
     * @return  <code>true</code> если указанный сериализер может применяться и для всех унаследованных классов от данного (если для них не был явно указан сериализер).
     */
    public boolean recursive() default false;

}
