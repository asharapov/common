package org.echosoft.common.query.providers;

import org.echosoft.common.query.BeanIterator;
import org.echosoft.common.query.Query;
import org.echosoft.common.query.QueryResult;

/**
 * Provides queried data using various rules, such as additional constraints on retrieving data,
 * sorting rules, paging support.
 * @author Anton Sharapov
 */
public interface DataProvider<T> {

    /**
     * Returns queried rows from the data provider.
     * @param query  optional parameter which can be add additional constraints, sorting rules
     *               or paging support for retrieved data.
     * @return lazy iterator through queried dataset.
     * @throws DataProviderException  in case if any errors occurs.
     */
    public BeanIterator<T> execute(Query query) throws DataProviderException;

    /**
     * Returns queried rows from the data provider.
     * @param query  optional parameter which can be add additional constraints, sorting rules
     *               or paging support for retrieved data.
     * @return range of the sorted records from the data set.
     * @throws DataProviderException  in case if any errors occurs.
     */
    public QueryResult<T> executePaged(Query query) throws DataProviderException;
}
