package org.echosoft.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.echosoft.common.dbf.DBFReader;
import org.echosoft.common.dbf.Field;

/**
 * @author Anton Sharapov
 */
public class DBFTest {

    public static void main(String[] args) throws Exception {
        final File file = new File("test.dbf").getCanonicalFile();
        if (!file.isFile())
            throw new RuntimeException("Can't find file " + file.getPath());
        try (InputStream in = new FileInputStream(file)) {
            final DBFReader reader = new DBFReader(in);
            final Field[] fields = reader.getFields();
            final int recordsCount = reader.getRecordsCount();
            if (reader.next()) {
                System.out.println("readed .");
            } else {
                System.out.println("NO DATA!");
            }
        }
    }

}
