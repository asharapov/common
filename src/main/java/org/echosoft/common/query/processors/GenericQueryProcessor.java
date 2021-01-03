package org.echosoft.common.query.processors;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.echosoft.common.io.FastStringWriter;
import org.echosoft.common.query.BeanIterator;
import org.echosoft.common.query.BeanLoader;
import org.echosoft.common.query.BeanMetaData;
import org.echosoft.common.query.Query;
import org.echosoft.common.query.QueryProcessor;
import org.echosoft.common.query.QueryResult;
import org.echosoft.common.query.SortCriterion;

/**
 * Generic implementation of the {@link QueryProcessor} interface. 
 * @author Anton Sharapov
 */
public class GenericQueryProcessor implements QueryProcessor {

    public static final int DEFAULT_FETCH_SIZE = 1000;

    private static final GenericQueryProcessor instance = new GenericQueryProcessor();
    public static QueryProcessor getInstance() {
        return instance;
    }
    private GenericQueryProcessor() {
    }

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
    public <T> QueryResult<T> executeQuery(String baseSQL, Query query, BeanLoader<T> loader, DataSource ds) throws SQLException {
        int rangeStart=0, rangeSize=0, estimatedTotalSize=0;
        final Object[] params;
        if (query!=null) {
            rangeSize = query.getPageSize()>0 ? query.getPageSize() : 0;
            rangeStart = query.getPageNumber()*rangeSize;
            estimatedTotalSize = query.getEstimatedTotalSize()>0 ? query.getEstimatedTotalSize() : 0;
            params = query.getParams();
        } else {
            params = new Object[0];
        }
        final int totalSize;
        final BeanMetaData metadata;
        final List<T> beans = new ArrayList<T>(rangeSize>0 ? Math.min(rangeSize,DEFAULT_FETCH_SIZE) : DEFAULT_FETCH_SIZE);

        final String sql = applyQuery(baseSQL, query, loader);
        final Connection conn = ds.getConnection();
        try {
            final PreparedStatement pstmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            try {
                pstmt.setFetchDirection(ResultSet.FETCH_FORWARD);
                pstmt.setFetchSize(rangeSize>0 ? Math.min(rangeSize,DEFAULT_FETCH_SIZE) : DEFAULT_FETCH_SIZE);
                for (int i=0; i<params.length; i++) {
                    pstmt.setObject(i+1, params[i]);
                }
                final ResultSet rs = pstmt.executeQuery();
                try {
                    if (rangeStart > 0)
                        rs.absolute(rangeStart);

                    if (loader instanceof JdbcBeanLoader) {
                        ((JdbcBeanLoader)loader).setMetaData(rs.getMetaData());
                    }
                    metadata = new BeanMetaData( loader.getMetadata() );

                    int readed = 0;
                    while (rs.next() && (rangeSize <=0 || readed++<rangeSize)) {
                        beans.add( loader.load(rs) );
                    }

                    if (rs.isLast()) {
                        totalSize = rs.getRow();
                    } else
                    if (estimatedTotalSize>0) {
                        totalSize = estimatedTotalSize;
                    } else {
                        rs.last();
                        totalSize = rs.getRow();
                    }

                    return new QueryResult<T>(beans, metadata, totalSize, rangeStart, rangeSize);
                } finally {
                    rs.close();
                }
            } finally {
                pstmt.close();
            }
        } catch (Exception e) {
            final SQLException se;
            if (e instanceof SQLException) {
                se = (SQLException)e;
            } else {
                se = new SQLException(e.getMessage());
                se.initCause(e);
            }
            throw se;
        } finally {
            conn.close();
        }
    }


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
    public <T> BeanIterator<T> executeQueryIterator(String baseSQL, Query query, BeanLoader<T> loader, DataSource ds) throws SQLException {
        int rangeStart=0, rangeSize=0;
        final Object[] params;
        if (query!=null) {
            rangeSize = query.getPageSize()>0 ? query.getPageSize() : 0;
            rangeStart = query.getPageNumber()*rangeSize;
            params = query.getParams();
        } else {
            params = new Object[0];
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            final String pageSQL = applyQuery(baseSQL, query, loader);
            conn = ds.getConnection();
            pstmt = conn.prepareStatement(pageSQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            pstmt.setFetchDirection(ResultSet.FETCH_FORWARD);
            pstmt.setFetchSize(rangeSize>0 ? Math.min(rangeSize,DEFAULT_FETCH_SIZE) : DEFAULT_FETCH_SIZE);
            for (int i=0; i<params.length; i++) {
                pstmt.setObject(i+1, params[i]);
            }
            rs = pstmt.executeQuery();
            if (loader instanceof JdbcBeanLoader) {
                ((JdbcBeanLoader)loader).setMetaData(rs.getMetaData());
            }

            return new JdbcBeanIterator<T>(conn, pstmt, rs, loader, rangeStart, rangeSize);

        } catch (Exception e) {
            if (rs!=null)
            try {
                rs.close();
            } catch (Throwable th) { th.printStackTrace(System.err); }
            if (pstmt!=null)
            try {
                pstmt.close();
            } catch (Throwable th) { th.printStackTrace(System.err); }
            if (conn!=null)
            try {
                conn.close();
            } catch (Throwable th) { th.printStackTrace(System.err); }

            final SQLException se;
            if (e instanceof SQLException) {
                se = (SQLException)e;
            } else {
                se = new SQLException(e.getMessage());
                se.initCause(e);
            }
            throw se;
        }
    }


    /**
     * Applies to base sql query all specified additional constraints.
     *
     * @param baseSQL  Base sql query.  Can't be null.
     * @param query  Additional constraints that should be applied to base sql query.  Can be null.
     * @param loader  Beans loader that responsible to convert resultset rows to specified beans. Can't be null.
     * @return  compiled sql query.
     */
    public String applyQuery(String baseSQL, Query query, BeanLoader loader) {
        if (query==null || (!query.hasConstraints() && query.getSortCriteria().isEmpty()))
            return baseSQL;

        final FastStringWriter out = new FastStringWriter();
        out.write("SELECT * FROM (\n");
        out.write(baseSQL);
        out.write("\n) QRSLT ");
        applyOrder(out, query, loader);
        return out.toString();
    }

    protected void applyOrder(FastStringWriter out, Query query, BeanLoader loader) {
        final SortCriterion order[] = query.getSortCriteria().toArray();
        if (order.length>0) {
            out.write(" ORDER BY ");
            for (int i=0; i<order.length; i++) {
                final String field = loader!=null ? loader.getMappedField(order[i].getField()) : order[i].getField();
                if (field==null)
                    throw new IllegalArgumentException("No mapped field for attribute "+order[i].getField());
                if (i>0)
                    out.write(',');
                out.write(field);
                out.write(order[i].isAscending() ? " ASC" : " DESC");
            }
        }
    }
}
