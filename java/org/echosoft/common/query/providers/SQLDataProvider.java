package org.echosoft.common.query.providers;

import javax.sql.DataSource;
import java.sql.SQLException;

import org.echosoft.common.query.BeanIterator;
import org.echosoft.common.query.BeanLoader;
import org.echosoft.common.query.Query;
import org.echosoft.common.query.QueryProcessor;
import org.echosoft.common.query.QueryResult;
import org.echosoft.common.query.processors.GenericQueryProcessor;
import org.echosoft.common.query.processors.SQLParamsTransformer;

/**
 * Implementation of the {@link DataProvider} which uses direct queries to a number of supported RDBMS.
 * An appropriated {@link QueryProcessor} implementation used for transforming abstract querying rules to
 * SQL language construction. 
 * @author Anton Sharapov
 */
public class SQLDataProvider<T> implements DataProvider {

    private final DataSource dataSource;
    private final QueryProcessor processor;
    private final BeanLoader<T> loader;
    private final String baseSQL;

    /**
     * Creates new instance of the sql data provider.
     * @param dataSource  connections data source. Must be specified.
     * @param processor  query processor. Must be specified.
     * @param loader  bean loader. Must be specified.
     * @param baseSQL  base sql expression.
     */
    public SQLDataProvider(DataSource dataSource, QueryProcessor processor, BeanLoader<T> loader, String baseSQL) {
        if (dataSource==null)
            throw new NullPointerException("DataSource must be specified");
        if (baseSQL == null || baseSQL.trim().length()==0)
            throw new NullPointerException("Query must be specified");
        if (loader==null)
            throw new NullPointerException("Bean loader must be specified");

        this.dataSource = dataSource;
        this.processor = processor!=null ? processor : GenericQueryProcessor.getInstance();
        this.loader = loader;
        this.baseSQL = baseSQL;
    }


    /**
     * Returns queried rows from the data provider.
     * @param query  optional parameter which can be add additional constraints, sorting rules
     *               or paging support for retrieved data.
     * @return range of the sorted records from the data set.
     * @throws DataProviderException  in case if any errors occurs.
     */
    public BeanIterator<T> execute(Query query) {
        try {
            final String baseSQL;
            if (query!=null && !query.getNamedParams().isEmpty()) {
                query = (Query)query.clone();
                final StringBuffer sqlBuf = new StringBuffer(this.baseSQL);
                SQLParamsTransformer.getInstance().transform(sqlBuf, query);
                baseSQL = sqlBuf.toString();
            } else {
                baseSQL = this.baseSQL;
            }
            return processor.executeQueryIterator(baseSQL, query, loader, dataSource);
        } catch (Exception e) {
            throw new DataProviderException(e.getMessage(), e);
        }
    }


    /**
     * Returns queried rows from the data provider.
     * @param query  optional parameter which can be add additional constraints, sorting rules
     *               or paging support for retrieved data.
     * @return range of the sorted records from the data set.
     * @throws DataProviderException  in case if any errors occurs.
     */
    public QueryResult<T> executePaged(Query query) throws DataProviderException {
        try {
            final String baseSQL;
            if (query!=null && !query.getNamedParams().isEmpty()) {
                query = (Query)query.clone();
                final StringBuffer sqlBuf = new StringBuffer(this.baseSQL);
                SQLParamsTransformer.getInstance().transform(sqlBuf, query);
                baseSQL = sqlBuf.toString();
            } else {
                baseSQL = this.baseSQL;
            }
            return processor.executeQuery(baseSQL, query, loader, dataSource);
        } catch (SQLException e) {
            throw new DataProviderException(e.getMessage(), e);
        }
    }
}
