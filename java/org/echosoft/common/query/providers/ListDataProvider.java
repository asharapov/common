package org.echosoft.common.query.providers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.echosoft.common.query.BeanComparator;
import org.echosoft.common.query.BeanIterator;
import org.echosoft.common.query.BeanMetaData;
import org.echosoft.common.query.ListBeanIterator;
import org.echosoft.common.query.Query;
import org.echosoft.common.query.QueryResult;

/**
 * <p><strong>ListDataProvider</strong> is a convenience implementation of
 * {@link DataProvider} that simple wraps a list of {@link org.echosoft.common.query.Row} instances.</p>
 * @author Anton Sharapov
 */
public class ListDataProvider<T> implements DataProvider {

    private final ArrayList<T> rows;
    private BeanMetaData metadata;

    public ListDataProvider() {
        rows = new ArrayList<T>();
    }

    /**
     * Creates data provider using given array of any types of java beans.
     * @param beans  an array of java beans that should be wrapped to corresponding {@link org.echosoft.common.query.Row} instances.
     */
    public ListDataProvider(T[] beans) {
        this.rows = new ArrayList<T>();
        this.rows.addAll( Arrays.asList(beans) );
    }

    /**
     * Creates data provider using given list of {@link org.echosoft.common.query.Row} instances or any other beans.
     * @param beans an array of java beans that should be wrapped to corresponding {@link org.echosoft.common.query.Row} instances.
     */
    public ListDataProvider(List<T> beans) {
        this.rows = new ArrayList<T>();
        this.rows.addAll( beans );
    }

    /**
     * Creates data provider using data passed through an iterator.
     * @param iter  iterator thru java beans (will be converted to {@link org.echosoft.common.query.Row}s).
     */
    public ListDataProvider(Iterator<T> iter) {
        this.rows = new ArrayList<T>();
        while (iter.hasNext()) {
            rows.add( iter.next() );
        }
    }


    private BeanMetaData getMetaData() {
        if (metadata==null) {
            final Object bean = rows.size()>0 ? rows.get(0) : null;
            metadata = BeanMetaData.resolveMetaData(bean);
        }
        return metadata;
    }

    /**
     * Provides direct access to all rows.
     * @return list of {@link org.echosoft.common.query.Row} instances.
     */
    public List<T> getRows() {
        return rows;
    }



    /**
     * Returns queried rows from the data provider.
     * @param query  optional parameter which can be add additional constraints, sorting rules
     *               or paging support for retrieved data.
     * @return range of the sorted records from the data set.
     * @throws DataProviderException  in case if any errors occurs.
     */
    public BeanIterator<T> execute(Query query) {
        final QueryResult<T> qr = executePaged(query);
        return new ListBeanIterator<T>(qr);
    }


    /**
     * Returns queried rows from this data provider.
     * @param query  optional parameter which can be add additional constraints, sorting rules
     *               or paging support for retrieved data.
     * @return range of the sorted records from the data set.
     */
    public QueryResult<T> executePaged(final Query query) {
        final int totalSize = rows.size();
        final int pageSize = query!=null ? query.getPageSize() : 0;
        final int from = query!=null ? query.getPageNumber()*pageSize : 0;
        if (from>totalSize) {
            final List<T> emptyList = Collections.emptyList();
            return new QueryResult<T>(emptyList);
        }

        List<T> list;
        if (query!=null && !query.getSortCriteria().isEmpty()) {
            list = new ArrayList<T>(rows.size());
            list.addAll(rows);
            Collections.sort(list, new BeanComparator<T>(query.getSortCriteria()));
        } else {
            list = rows;
        }

        if (pageSize>0) {
            final int realCount = from+pageSize>totalSize ? totalSize - from : pageSize;
            list = list.subList(from, from+realCount);
        }
        return new QueryResult<T>(list, getMetaData(), totalSize, from, pageSize);
    }

}
