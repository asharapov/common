package org.echosoft.common.cli.parser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Описание коллекции опций которые могут присутствовать среди аргументов командной строки.
 * @author Anton Sharapov
 */
public class Options implements Serializable {

    private final List<Option> options;
    private final Map<String,Option> index;

    public Options() {
        this.options = new ArrayList<Option>();
        this.index = new HashMap<String,Option>();
    }

    /**
     * Возвращает итератор по всем зарегистрированным в коллекции опциям которые могут встречаться в командной строке.
     * @return все зарегистрированные в коллекции опции.
     */
    public Iterable<Option> getOptions() {
        return options;
    }

    /**
     * Возвращает <code>true</code> если опция с данным именем зарегистрирована в коллекции.
     * @param option проверяемая опция.
     * @return <code>true</code> если указанная опция зарегистрирована в коллекции.
     */
    public boolean hasOption(final Option option) {
        return options.contains(option);
    }

    /**
     * Возвращает <code>true</code> если опция с данным именем зарегистрирована в коллекции.
     * @param optionName краткое или полное название опции.
     * @return <code>true</code> если указанная опция зарегистрирована в коллекции.
     */
    public boolean hasOption(final String optionName) {
        return index.containsKey(optionName);
    }

    /**
     * Возвращает полное описание опции по ее полному либо краткому имени.
     * @param optionName краткое или полное название опции.
     * @return <code>true</code> если опция с таким названием уже зарегистрирована в коллекции.
     */
    public Option getOption(final String optionName) {
        return index.get(optionName);
    }

    /**
     * Возвращает полное описание опции по ее полному либо краткому имени.
     * @param optionName краткое или полное название опции.
     * @return <code>true</code> если опция с таким названием уже зарегистрирована в коллекции.
     */
    public Option getOption(final char optionName) {
        return index.get(String.valueOf(optionName));
    }

    /**
     * Регистрирует новую опцию в коллекции.
     * @param option  описание новой опции.
     * @throws IllegalArgumentException  поднимается в случае когда опция с таким кратким или полным именем уже зарегистрирована в коллекции.
     */
    public void addOption( final Option option ) {
        if (option.getShortName()!=null && index.containsKey(option.getShortName().toString()))
            throw new IllegalArgumentException("Option '"+option.getShortName()+"' already registered in the collection");
        if (option.getFullName()!=null && index.containsKey(option.getFullName()))
            throw new IllegalArgumentException("Option '"+option.getFullName()+"' already registered in the collection");
        options.add( option );
        if (option.getShortName()!=null)
            index.put(option.getShortName().toString(), option);
        if (option.getFullName()!=null)
            index.put(option.getFullName(), option);
    }
}
