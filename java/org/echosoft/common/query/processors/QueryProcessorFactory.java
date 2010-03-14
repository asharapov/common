package org.echosoft.common.query.processors;

import java.util.HashMap;
import java.util.Map;

import org.echosoft.common.query.QueryProcessor;

/**
 * @author Anton Sharapov
 */
public class QueryProcessorFactory {

    private static final QueryProcessorFactory instance = new QueryProcessorFactory();
    public static QueryProcessorFactory getInstance() {
        return instance;
    }

    private final Map<String, QueryProcessor> processors;

    private QueryProcessorFactory() {
        processors = new HashMap<String,QueryProcessor>();
        processors.put("oracle", OracleQueryProcessor.getInstance());
        processors.put("pgsql", PGSQLQueryProcessor.getInstance());
        processors.put("mssql", MSSQLQueryProcessor.getInstance());
        processors.put("generic", GenericQueryProcessor.getInstance());
    }


    public QueryProcessor getProcessor(final String name) {
        return processors.get(name);
    }

    public void registerProcessor(final String name, final QueryProcessor processor) {
        if (name==null || processor==null)
            throw new IllegalArgumentException("All arguments must be specified");
        processors.put(name, processor);
    }
}
