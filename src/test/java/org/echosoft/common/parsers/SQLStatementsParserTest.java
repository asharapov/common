package org.echosoft.common.parsers;

import java.io.StringReader;

import org.junit.Test;

/**
 * @author Anton Sharapov
 */
public class SQLStatementsParserTest {

    private static final String SCRIPT = "-- statement 1;\n" +
            "/**\n" +
            " * First /query/:\n" +
            " */\n" +
            "SELECT a,b,c \n" +
            "FROM table1\n" +
            "WHERE a > :amount  -- check parameter value\n" +
            "\n" +
            "/\n" +
            "/\n" +
            "-- statement 2;\n" +
            "/*\n" +
            " Second /query':\n" +
            " */\n" +
            "INSERT INTO table1(a,b,c) VALUES(?,?,?);\n" +
            "INSERT INTO table2(x,y,z) VALUES(?,?,'test;/') RETURNING ?;\n" +
            "\n" +
            "-- statement 4;\n" +
            "DECLARE id VARCHAR2(16)\n" +
            "DECLARE amt NUMBER(10)\n" +
            "BEGIN\n" +
            "  if :amt/2 >1 then\n" +
            "    BEGIN\n" +
            "    END;\n" +
            "  end if;\n" +
            "END;\n" +
            "\n" +
            "CREATE OR REPLACE TRIGGER KOSGU.TBI_BASE_DOCUMENT\n" +
            "BEFORE INSERT\n" +
            "ON KOSGU.BASE_DOCUMENT\n" +
            "FOR EACH ROW\n" +
            "BEGIN\n" +
            "  if :new.ID is null then\n" +
            "    select SEQ_ID.NEXTVAL into :new.ID from dual;\n" +
            "  end if;\n" +
            "END;\n" +
            "\n" +
            "DECLARE\n" +
            "  emp_id          employees_temp.employee_id%TYPE := 299;\n" +
            "  emp_first_name  employees_temp.first_name%TYPE  := 'Bob';\n" +
            "  emp_last_name   employees_temp.last_name%TYPE   := 'Henry';\n" +
            "BEGIN\n" +
            "  INSERT INTO employees_temp (employee_id, first_name, last_name) \n" +
            "  VALUES (emp_id, emp_first_name, emp_last_name);\n" +
            " \n" +
            "  UPDATE employees_temp\n" +
            "  SET first_name = 'Robert'\n" +
            "  WHERE employee_id = emp_id;\n" +
            " \n" +
            "  DELETE FROM employees_temp\n" +
            "  WHERE employee_id = emp_id\n" +
            "  RETURNING first_name, last_name\n" +
            "  INTO emp_first_name, emp_last_name;\n" +
            " \n" +
            "  COMMIT;\n" +
            "  DBMS_OUTPUT.PUT_LINE (emp_first_name || ' ' || emp_last_name);\n" +
            "END;" +
            "";


    @Test
    public void test1() throws Exception {
        SQLStatementsParser parser = new SQLStatementsParser(new StringReader(SCRIPT));
        while (parser.hasNext()) {
            final String stmt = parser.next();
            System.out.println(stmt);
            System.out.println("*** *** ***");
        }
    }


    private static final String SCRIPT2 =
            "DECLARE amt NUMBER(10)\n" +
                    "BEGIN\n" +
                    "  if :amt/2 >1 then\n" +
                    "    BEGIN\n" +
                    "    END;\n" +
                    "  end if;\n" +
                    "END;\nSECOND STATEMENT";

    @Test
    public void test2() throws Exception {
        SQLStatementsParser parser = new SQLStatementsParser(new StringReader(SCRIPT2));
        while (parser.hasNext()) {
            final String stmt = parser.next();
            System.out.println(stmt);
            System.out.println("*** *** ***");
        }
    }
}
