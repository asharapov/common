package org.echosoft.common.json;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;

/**
 * Данный интерфейс отвечает за корректное формирование JSON выражений на основе объектов java.<br/>
 * <b>Внимание!</b> Экземпляры данного класса не являются потокобезопасными и должны использоваться только из одного потока в каждую единицу времени.
 *
 * @author Anton Sharapov
 */
public interface JsonWriter {

    /**
     * Сигнализирует о начале обработки (помещения в поток) некоторого массива данных (согласно принятым в javascript правилам).
     *
     * @throws IOException           в случае каких-либо проблем с вводом-выводом.
     * @throws IllegalStateException если в данный момент начало обработки нового массива недопустимо.
     */
    public void beginArray() throws IOException;

    /**
     * Сигнализирует о завершении обработки текущего массива данных.
     *
     * @throws IOException           в случае каких-либо проблем с вводом-выводом.
     * @throws IllegalStateException если в данный момент завершение обработки текущего массива недопустимо (например если он и не начинал обрабатываться).
     */
    public void endArray() throws IOException;

    /**
     * Помещает в поток указанный объект не вдаваясь в подробности о его структуре. Это может быть либо null, либо простое значение одного из регулярных типов,
     * либо некий сложный объект либо массив объектов.<br/>
     * Вызов данного метода является предпочтительным когда надо сериализовать в JSON формат некоторый существующий java объект.
     * В противном случае, когда JSON выражение формируется динамически, то может быть целесообразнее использовать
     * комбинацию методов {@link #beginObject()}, {@link #writeProperty(String, Object)}, {@link #endObject()} итп.
     *
     * @param obj java объект который должен быть транслирован в формат JSON и помещен в выходной поток.
     * @throws IOException               в случае каких-либо проблем с вводом-выводом.
     * @throws IllegalStateException     если в данный момент помещение нового объекта в поток недопустимо.
     * @throws IllegalAccessException    в случае когда вызывающий код не имеет достаточно прав для обращения к свойствам сериализуемого объекта.
     * @throws InvocationTargetException в случае если при обращении к свойствам сериализуемого объекта произошла ошибка.
     */
    public void writeObject(Object obj) throws InvocationTargetException, IllegalAccessException, IOException;

    /**
     * Сигнализирует о начале обработки (помещения в поток) некоторого объекта (согласно принятым в javascript правилам).
     *
     * @throws IOException           в случае каких-либо проблем с вводом-выводом.
     * @throws IllegalStateException если в данный момент начало обработки нового объекта недопустимо.
     */
    public void beginObject() throws IOException;

    /**
     * Сигнализирует о завершении обработки текущего объекта.
     *
     * @throws IOException           в случае каких-либо проблем с вводом-выводом.
     * @throws IllegalStateException если в данный момент завершение обработки текущего объекта недопустимо (например если он и не начинал обрабатываться).
     */
    public void endObject() throws IOException;

    /**
     * Помещает в поток имя свойства JSON объекта и его значение. Значением свойства может быть либо null, либо простое значение одного из регулярных типов
     * либо некий сложный составной объект либо массив объектов. При необходимости, имя свойства и его значение могут быть подвергнуты
     * дополнительным преобразованиям для обеспечения совместимости с JSON форматом перед помещением их в выходной поток.<br>
     * Данный метод допускается вызывать только между вызовами {@link #beginObject()} и {@link #endObject()} в рамках одного и того же контекста.
     *
     * @param name  строка с именем свойства объекта.
     * @param value значение данного свойства объекта.
     * @throws IOException               в случае каких-либо проблем с вводом-выводом.
     * @throws IllegalStateException     если в данный момент недопустима установка свойства объекта (например если объект и не начинал обрабатываться).
     * @throws IllegalAccessException    в случае когда вызывающий код не имеет достаточно прав для обращения к свойствам сериализуемого объекта.
     * @throws InvocationTargetException в случае если при обращении к свойствам сериализуемого объекта произошла ошибка.
     */
    public void writeProperty(String name, Object value) throws InvocationTargetException, IllegalAccessException, IOException;

    /**
     * Начинает запись в поток значения указанного свойства объекта. Данный метод используется в тесной связке с методами {@link #beginObject()} или {@link #beginArray()}.
     * Данный метод как и {@link #writeProperty(String, Object)}  применяется для динамического формирования JSON выражения.</br>
     * пример использования:<br/>
     * <pre><code>
     *  JspWriter w = ...
     *  w.beginObject();
     *  w.writeProperty("total", 3);
     *  w.writeComplexProperty("items");
     *  w.beginArray();
     *  w.writeObject("item1");
     *  w.writeObject( new Date() );
     *  w.write.Object(2.3);
     *  w.endArray();
     *  w.endObject();
     * </code></pre>
     * сформирует примерно следующее JSON выражение:
     * <code>{total:3, items:["item1", new Date(2009,01,20,00,00,00),2.3]}</code><br/>
     * Тот же самый пример можно переписать с использованием только метода {@link #writeProperty(String, Object)}:<br/>
     * <pre><code>
     *  JspWriter w = ...
     *  w.beginObject();
     *  w.writeProperty("total", 3);
     *  Object[] items = new Object[]{"item1", new Date(), 2.3};
     *  w.writeProperty("items", items);
     *  w.endObject();
     * </code></pre>
     *
     * @param name имя свойства объекта.
     * @throws IOException           в случае каких-либо проблем с вводом-выводом.
     * @throws IllegalStateException если в данный момент недопустима установка свойства объекта (например если объект и не начинал обрабатываться).
     */
    public void writeComplexProperty(String name) throws IOException;

    //
    // методы для внутреннего использования (перенести в отдельный интерфейс ?):
    //

    /**
     * Возвращает глобальный контекст с которым ассоциирован данный класс.
     * Данный метод предназначен для внутреннего использования в классах, реализующих интерфейс {@link JsonSerializer}.
     *
     * @return экземпляр контекста. Не может быть <code>null</code>.
     */
    public JsonContext getContext();

    /**
     * Возвращает выходной поток куда записывается результат сериализации объектов в JSON формат.
     * Данный метод предназначен для внутреннего использования в классах, реализующих интерфейс {@link JsonSerializer}.
     *
     * @return выходной поток для результата. Не может быть <code>null</code>.
     */
    public Writer getOutputWriter();
}
