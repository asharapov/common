package org.echosoft.common.cli;

import org.echosoft.common.cli.parser.CLParser;
import org.echosoft.common.cli.parser.CLPrintUtils;
import org.echosoft.common.cli.parser.CommandLine;
import org.echosoft.common.cli.parser.Option;
import org.echosoft.common.cli.parser.Options;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class CLParserTest {

    private static Options options1;
    private static Options options2;

    @BeforeClass
    public static void setup() {
        options1 = new Options();
        options1.addOption(new Option(null, "cfg-dir", false, "dir", "Каталог с конфигурационными файлами. Значение по умолчанию: 'config'."));
        options1.addOption(new Option('g', "group", false, "id", "Идентификатор группы ПАК."));
        options1.addOption(new Option('i', "instance", false, "id", "Идентификатор определенного ПАК в группе. Формат: '<group id>:<instance id>[,<instance id>]'."));
        options1.addOption(new Option('p', "pause", "Делает паузу при старте программы. Используется в отладочных целях."));
        options1.addOption(new Option('v', "version", "Печатает версию программы и завершает ее выполнение."));
        options1.addOption(new Option('h', "help", "Печатает данные подсказки по программе и завершает ее выполнение."));
        options1.addOption(new Option('d', null, "Используется в отладочных целях."));
        options1.addOption(new Option('q', null, false, "arg", "Используется в отладочных целях."));

        options2 = new Options();
        options2.addOption(new Option('j', "jobs", false, "cnt", "Задает количество параллельных потоков в которых выполняется задача. Значение по умолчанию: '99'."));
        options2.addOption(new Option('p', "protocol", false, "file", "Файл в который будет помещен протокол прохождения всех тестов."));
        options2.addOption(new Option('e', "log-errors-only", "При задании этой опции в итоговый протокол будут помещаться только записи о найденных ошибках."));
    }

    @Test
    public void testOptions1() throws Exception {
        final String[] args = {"-g", "rbduig", "-hxyz", "test", "-e", "-p", "-j", "99"};
        final CommandLine line1 = new CLParser(options1, false).parse(args);
        System.out.println(line1);
    }

    @Test
    public void testPrintOptions() throws Exception {
        CLPrintUtils.printHelp(options1,  System.out, 100);
    }

}
