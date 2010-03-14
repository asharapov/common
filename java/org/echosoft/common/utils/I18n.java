package org.echosoft.common.utils;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Данный класс представляет собой простой менеджер ресурсов и предназначен для упрощения работы с локализованными сообщениями.
 * 
 * @author Anton Sharapov
 */
public class I18n {

    public static final char ALIAS_SEPARATOR = '@';
    public static final String DEFAULT_ALIAS = "";
    private static final ConcurrentHashMap<String,String> resources = new ConcurrentHashMap<String,String>();
    private static final ConcurrentHashMap<BundleKey,ResourceBundle> bundles = new ConcurrentHashMap<BundleKey,ResourceBundle>();

    /**
     * Регистрирует в данном классе путь к некоторому ресурсу под определенным синонимом.
     * В дальнейшем, доступ к сообщениям из данного ресурса будет осуществляться уже с использованием данного синонима.
     * @param alias синоним к указанному ресурсу.
     * @param resource полный путь к ресурсу в CLASSPATH. 
     * @throws IllegalArgumentException в случае если путь к ресурсу не был указан или под указанным синонимом уже был зарегистрирован другой ресурс.
     */
    public static void registerBundle(String alias, final String resource) {
        if (alias==null)
            alias = DEFAULT_ALIAS;
        if (resource==null)
            throw new IllegalArgumentException("Resource path must be specified.");
        final String r = resources.get(alias);
        if (r!=null && !resource.equals(r))
            throw new IllegalArgumentException("Resource bundle with the alias ["+alias+"] already registered.");
        resources.put(alias, resource);
    }

    /**
     * Возвращает локализованное сообщение, соответствующее указанному в аргументе ключу и локали.
     * @param key ключ к сообщению в определенном {@link ResourceBundle}. Данный аргумент имеет формат <code>[alias@]msgkey</code><br/> где
     * <ul>
     *  <li> <code>alias</code> - синоним, под которой в данном классе был зарегистрирован требуемый нам {@link ResourceBundle}.
     *  Если синоним не указан то считается что он равен пустой строке, т.е. будет использоваться тот ресурс что был зарегистрирован в этом классе под таким именем.
     *  <li> <code>msgkey</code> - идентификатор искомого сообщения в {@link ResourceBundle}.
     * </ul>
     * @param locale определяет локаль для которой нам надо вернуть сообщение.
     * @return локализованное сообщение.
     * @throws java.util.MissingResourceException в случае когда экземпляр {@link ResourceBundle} не содержит сообщения с заданным ключом.
     */
    public static String getMessage(String key, final Locale locale) {
        final String alias;
        final int p = key.indexOf(ALIAS_SEPARATOR);
        if (p>=0) {
            alias = key.substring(0, p);
            key = key.substring(p+1);
        } else {
            alias = DEFAULT_ALIAS;
        }
        return getMessage(alias, key, null, locale);
    }

    /**
     * Возвращает локализованное сообщение, соответствующее указанному в аргументе ключу и локали.
     * @param key ключ к сообщению в определенном {@link ResourceBundle}. Данный аргумент имеет формат <code>[alias@]msgkey</code><br/> где
     * <ul>
     *  <li> <code>alias</code> - синоним, под которой в данном классе был зарегистрирован требуемый нам {@link ResourceBundle}.
     *  Если синоним не указан то считается что он равен пустой строке, т.е. будет использоваться тот ресурс что был зарегистрирован в этом классе под таким именем.
     *  <li> <code>msgkey</code> - идентификатор искомого сообщения в {@link ResourceBundle}.
     * </ul>
     * @param params  массив объектов, значения которых будут использоваться при форматировании полученной из {@link ResourceBundle} локализованной строки.
     * @param locale определяет локаль для которой нам надо вернуть сообщение.
     * @return локализованное сообщение.
     * @throws java.util.MissingResourceException в случае когда экземпляр {@link ResourceBundle} не содержит сообщения с заданным ключом.
     */
    public static String getMessage(String key, Object params[], final Locale locale) {
        final String alias;
        final int p = key.indexOf(ALIAS_SEPARATOR);
        if (p>=0) {
            alias = key.substring(0, p);
            key = key.substring(p+1);
        } else {
            alias = DEFAULT_ALIAS;
        }
        return getMessage(alias, key, params, locale);
    }

    /**
     * Возвращает локализованное сообщение, соответствующее указанному в аргументе ключу и локали.
     * @param alias идентификатор экземпляра {@link ResourceBundle} содержащего локализованные версии нужного нам сообщения.
     * @param key  идентификатор нужного нам сообщения в соответствующем {@link ResourceBundle}.
     * @param locale определяет локаль для которой нам надо вернуть сообщение.
     * @return локализованное сообщение.
     * @throws java.util.MissingResourceException в случае когда экземпляр {@link ResourceBundle} не содержит сообщения с заданным ключом.
     */
    public static String getMessage(final String alias, final String key, final Locale locale) {
        return getMessage(alias, key, null, locale);
    }

    /**
     * Возвращает локализованное сообщение, соответствующее указанному в аргументе ключу и локали.
     * @param alias идентификатор экземпляра {@link ResourceBundle} содержащего локализованные версии нужного нам сообщения.
     * @param key  идентификатор нужного нам сообщения в соответствующем {@link ResourceBundle}.
     * @param params  массив объектов, значения которых будут использоваться при форматировании полученной из {@link ResourceBundle} локализованной строки.
     * @param locale определяет локаль для которой нам надо вернуть сообщение.
     * @return локализованное сообщение.
     * @throws java.util.MissingResourceException в случае когда экземпляр {@link ResourceBundle} не содержит сообщения с заданным ключом.
     */
    public static String getMessage(final String alias, final String key, final Object params[], final Locale locale) {
        final String resource = resources.get(alias);
        if (resource==null)
            throw new IllegalArgumentException("No resource found for alias: "+alias);
        final BundleKey desc = new BundleKey(resource, locale);
        ResourceBundle bundle = bundles.get(desc);
        if (bundle==null) {
            bundle = ResourceBundle.getBundle(resource, locale);
            bundles.put(desc, bundle);
        }
        //final ResourceBundle bundle = ResourceBundle.getBundle((String)resources.get(alias), locale);
        final String message = bundle.getString(key);
        return params==null || params.length==0
                ? message
                : new MessageFormat(message, locale).format(params);
    }

    /**
     * Оборачивает указанную в аргументе строку в объект реализующий интерфейс {@link Message}.
     * @param text  любой текст.
     * @return экземпляр {@link Message} возвращающий указанный в аргументе текст для любой локали. 
     */
    public static Message makeStaticMessage(final String text) {
        return new StaticMessage(text);
    }

    /**
     *
     * @param key  ключ сообщения в соответствующем файле ресурсов.
     * @return экземпляр {@link Message}
     */
    public static Message makeMessage(final String key) {
        return new DynaMessage(key);
    }

    /**
     *
     * @param key  ключ сообщения в соответствующем файле ресурсов.
     * @param params  опциональный массив параметров для формирования локализованной строки.
     * @return экземпляр {@link Message}
     */
    public static Message makeMessage(final String key, final Object... params) {
        return new DynaMessage(key, params);
    }



    private static final class BundleKey {
        private final String resource;
        private final Locale locale;
        private BundleKey(final String resource, final Locale locale) {
            this.resource = resource;
            this.locale = locale;
        }
        public int hashCode() {
            return resource.hashCode();
        }
        public boolean equals(final Object obj) {
            if (obj==null || !getClass().equals(obj.getClass()))
                return false;
            final BundleKey other = (BundleKey)obj;
            return resource.equals(other.resource) && locale.equals(other.locale);
        }
        public String toString() {
            return "{resource:"+resource+", locale:"+locale+"}";
        }
    }

    /**
     * Описывает определенное сообщение которое можно отображать на нескольких языках.
     */
    public static interface Message extends Serializable {
        /**
         * Возвращает сообщение с использованием локали по умолчанию.
         * @return  локализованное сообщение.
         */
        public String getString();
        /**
         * Возвращает сообщение с использованием указанной локали.
         * @param locale  используемая локаль.
         * @return  локализованное сообщение.
         */
        public String getString(Locale locale);
    }

    /**
     * Реализация {@link Message} используемая для указания статичного текста, независимого от локали.
     * Применяется в случаях когда надо адаптировать простой текст под интерфейс {@link Message}.
     */
    private static final class StaticMessage implements Message {
        private final String text;
        private StaticMessage(final String text) {
            this.text = text;
        }
        public String getString() {
            return text;
        }
        public String getString(final Locale locale) {
            return text;
        }
        public int hashCode() {
            return text!=null ? text.hashCode() : 0;
        }
        public boolean equals(final Object obj) {
            if (obj==null || !getClass().equals(obj.getClass()))
                return false;
            final StaticMessage other = (StaticMessage)obj;
            return text!=null ? text.equals(other.text) : other.text==null;
        }
        public String toString() {
            return "[I18n.StaticMessage{text:"+text+"}]";
        }
    }

    /**
     * Основная реализация {@link Message} используемая для получения различных представлений сообщения в зависимости от указанной в аргументах локали.
     */
    private static final class DynaMessage implements Message {
        private final String key;
        private final Object params[];
        private DynaMessage(final String key) {
            if (key==null)
                throw new IllegalArgumentException("Resource key must be specified");
            this.key = key;
            this.params = null;
        }
        private DynaMessage(final String key, final Object... params) {
            if (key==null)
                throw new IllegalArgumentException("Resource key must be specified");
            this.key = key;
            this.params = params.length>0 ? params : null;
        }
        public String getString() {
            return I18n.getMessage(key, params, Locale.getDefault());
        }
        public String getString(final Locale locale) {
            return I18n.getMessage(key, params, locale);
        }
        public int hashCode() {
            return key.hashCode();
        }
        public boolean equals(final Object obj) {
            if (obj==null || !getClass().equals(obj.getClass()))
                return false;
            final DynaMessage other = (DynaMessage)obj;
            return key.equals(other.key) &&
                    Arrays.equals(params, other.params);
        }
        public String toString() {
            return "[I18n.DynaMessage{key:"+key+", params:"+Arrays.toString(params)+"}]";
        }
    }
}
