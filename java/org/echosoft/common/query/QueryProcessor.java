package org.echosoft.common.query;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * 
 * @author Anton Sharapov
 */
public interface QueryProcessor {

    /**
     * Loads and retrieves all data from storage that conforms specified query constraints.
     *
     * @param baseSQL  Base sql query.  Can't be null.
     * @param query  Additional constraints that should be applied to base sql query.  Can be null.
     * @param loader  Beans loader that responsible to convert resultset rows to specified beans. Can't be null.
     * @param ds  Data source. Can't be null.
     * @return a corresponding {@link QueryResult} instance. This method never returns null.
     * @throws SQLException  if any errors occurs.
     */
    public <T> QueryResult<T> executeQuery(String baseSQL, Query query, BeanLoader<T> loader, DataSource ds) throws SQLException;

    /**
     * Retrieves iterator through data that conforms specified query constraints.
     * After using, this iterator should be manually closed.
     *
     * @param baseSQL  Base sql query.  Can't be null.
     * @param query  Additional constraints that should be applied to base sql query.  Can be null.
     * @param loader  Beans loader that responsible to convert resultset rows to specified beans. Can't be null.
     * @param ds  Data source. Can't be null.
     * @return a corresponding {@link BeanIterator} instance. This method never returns null.
     * @throws SQLException  if any errors occurs.
     */
    public <T> BeanIterator<T> executeQueryIterator(String baseSQL, Query query, BeanLoader<T> loader, DataSource ds) throws SQLException;

    /**
     * Applies to base sql query all specified additional constraints.
     *
     * @param baseSQL  Base sql query.  Can't be null.
     * @param query  Additional constraints that should be applied to base sql query.  Can be null.
     * @param loader  Beans loader that responsible to convert resultset rows to specified beans. Can't be null.
     * @return  compiled sql query.
     */
    public String applyQuery(String baseSQL, Query query, BeanLoader loader);
}
