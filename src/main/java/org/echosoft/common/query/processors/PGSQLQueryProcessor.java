package org.echosoft.common.query.processors;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.echosoft.common.io.FastStringWriter;
import org.echosoft.common.query.BeanIterator;
import org.echosoft.common.query.BeanLoader;
import org.echosoft.common.query.BeanMetaData;
import org.echosoft.common.query.Query;
import org.echosoft.common.query.QueryConstraint;
import org.echosoft.common.query.QueryOperator;
import org.echosoft.common.query.QueryProcessor;
import org.echosoft.common.query.QueryResult;
import org.echosoft.common.query.SortCriterion;

/**
 * This implementation of the {@link QueryProcessor} adapted for the postgresql database.
 * @author Anton Sharapov
 */
public class PGSQLQueryProcessor implements QueryProcessor {

    public static final int DEFAULT_FETCH_SIZE = 1000;

    private static final PGSQLQueryProcessor instance = new PGSQLQueryProcessor();
    public static QueryProcessor getInstance() {
        return instance;
    }
    private PGSQLQueryProcessor() {
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
            final PreparedStatement pstmt1 = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            try {
                pstmt1.setFetchDirection(ResultSet.FETCH_FORWARD);
                pstmt1.setFetchSize(rangeSize>0 ? Math.min(rangeSize,DEFAULT_FETCH_SIZE) : DEFAULT_FETCH_SIZE);
                for (int i=0; i<params.length; i++) {
                    pstmt1.setObject(i+1, params[i]);
                }
                final ResultSet rs = pstmt1.executeQuery();
                try {
                    if (loader instanceof JdbcBeanLoader) {
                        ((JdbcBeanLoader)loader).setMetaData(rs.getMetaData());
                    }
                    metadata = new BeanMetaData( loader.getMetadata() );
                    while (rs.next()) {
                        beans.add( loader.load(rs) );
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
                    rs.close();
                }
            } finally {
                pstmt1.close();
            }

            if (estimatedTotalSize>0) {
                totalSize = estimatedTotalSize;
            } else
            if (rangeSize>0) {
                final String totalCountSQL = getTotalRowCountQuery(baseSQL, query, loader);
                final PreparedStatement pstmt2 = conn.prepareStatement(totalCountSQL);
                try {
                    for (int i=0; i<params.length; i++) {
                        pstmt2.setObject(i+1, params[i]);
                    }
                    final ResultSet rs = pstmt2.executeQuery();
                    try {
                        totalSize = rs.next() ? rs.getInt(1) : 0;
                    } finally {
                        rs.close();
                    }
                } finally {
                    pstmt2.close();
                }
            } else {
                totalSize = beans.size();
            }

            return new QueryResult<T>(beans, metadata, totalSize, rangeStart, rangeSize);
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

        final FastStringWriter out = new FastStringWriter(32);
        out.write("SELECT * FROM (\n");
        out.write(baseSQL);
        out.write("\n) qrslt\n");

        if (query.hasConstraints()) {
            out.write("WHERE ");
            for (Iterator i = query.constraints(); i.hasNext(); ) {
                final QueryConstraint constraint = (QueryConstraint)i.next();
                final String field = loader!=null ? loader.getMappedField(constraint.getAttrName()) : constraint.getAttrName();
                if (field==null)
                    throw new IllegalArgumentException("No mapped field for attribute "+constraint.getAttrName());
                applyConstraint(out, constraint, field);
                if (i.hasNext())
                    out.write(" AND ");
            }
        }
        applyOrder(out, query, loader);
        if (query.getPageSize()>0) {
            out.write(" LIMIT ");
            out.write(Integer.toString(query.getPageSize()));
            if (query.getPageNumber()>=0) {
                out.write(" OFFSET ");
                out.write(Integer.toString(query.getPageNumber()*query.getPageSize()));
                out.write(' ');
            }
        }
        return out.toString();
    }

    protected String getTotalRowCountQuery(String baseSQL, Query query, BeanLoader loader) throws SQLException {
        final FastStringWriter out = new FastStringWriter();
        out.write("SELECT count(*) FROM (\n");
        out.write(baseSQL);
        out.write(") qry \n");
        if (query!=null && query.hasConstraints()) {
            out.write("WHERE ");
            for (Iterator i = query.constraints(); i.hasNext(); ) {
                final QueryConstraint constraint = (QueryConstraint)i.next();
                final String field = loader!=null ? loader.getMappedField(constraint.getAttrName()) : constraint.getAttrName();
                if (field==null)
                    throw new IllegalArgumentException("No mapped field for attribute "+constraint.getAttrName());
                applyConstraint(out, constraint, field);
                if (i.hasNext())
                    out.write(" AND ");
            }
        }
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

    protected void applyConstraint(FastStringWriter out, QueryConstraint constraint, String fieldName) {
        if (constraint.isNegative())
            out.write("NOT ");
        if (!constraint.isCaseSensitive()) {
            out.write("UPPER(");
            out.write(fieldName);
            out.write(") ");
        } else {
            out.write(fieldName);
            out.write(' ');
        }

        final QueryOperator operator = constraint.getOperator();
        final Object value = constraint.getValue();

        if (value==null) {
            out.write("IS NULL ");
            return;
        }

        if (QueryOperator.IN.equals(operator)) {
            if (value instanceof Collection) {
                out.write("IN ");
                encodeCollection(out, (Collection)value, !constraint.isCaseSensitive());
            } else
            if (value instanceof Object[]) {
                out.write("IN ");
                encodeCollection(out, (Object[])value, !constraint.isCaseSensitive());
            } else {
                out.write("IN (");
                out.write(value.toString());
                out.write(')');
            }
        } else
        if (QueryOperator.EQUAL.equals(operator)) {
            if (value instanceof Collection) {
                out.write("IN ");
                encodeCollection(out, (Collection)value, !constraint.isCaseSensitive());
            } else
            if (value instanceof Object[]) {
                out.write("IN ");
                encodeCollection(out, (Object[])value, !constraint.isCaseSensitive());
            } else {
                out.write("= ");
                encodeValue(out, value, !constraint.isCaseSensitive());
            }
        } else {
            out.write(operator.getName());
            out.write(' ');
            encodeValue(out, value, !constraint.isCaseSensitive());
        }
    }

    protected void encodeValue(FastStringWriter out, Object value, boolean upcase) {
        if (value instanceof String) {
            encodeString(out, (String)value, upcase);
        } else
        if (value instanceof java.sql.Date) {
            encodeDate(out, (java.sql.Date)value);
        } else
        if (value instanceof Date) {
            encodeDateTime(out, (Date)value);
        } else
        if (value instanceof Calendar) {
            encodeDateTime(out, ((Calendar)value).getTime());
        } else
        if (value instanceof Boolean) {
            out.write( ((Boolean)value).booleanValue() ? "1" : "0" );
        } else {
            out.write(value!=null ? value.toString() : "null");
        }
    }

    protected void encodeCollection(FastStringWriter out, Collection values, boolean upcase) {
        if (values.isEmpty()) {
            out.write("(null)");
            return;
        }

        out.write('(');
        for (Iterator i=values.iterator(); i.hasNext(); ) {
            encodeValue(out, i.next(), upcase);
            if (i.hasNext())
                out.write(',');
        }
        out.write(')');
    }

    protected void encodeCollection(FastStringWriter out, Object values[], boolean upcase) {
        if (values.length==0) {
            out.write("(null)");
            return;
        }

        out.write('(');
        for (int i=0; i<values.length; i++ ) {
            if (i>0)
                out.write(',');
            encodeValue(out, values[i], upcase);
        }
        out.write(')');
    }

    protected void encodeString(FastStringWriter out, String value, boolean upcase) {
        final int length = value.length();
        out.write('\'');
        for (int i=0; i<length; i++) {
            final char c = value.charAt(i);
            out.write(upcase ? Character.toUpperCase(c) : c);
            if (c=='\'')
                out.write('\'');
        }
        out.write('\'');
    }

    protected void encodeDate(FastStringWriter out, Date value) {
        final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        out.write("TO_DATE('");
        out.write(formatter.format(value));
        out.write("', 'DD.MM.YYYY')");
    }

    protected void encodeDateTime(FastStringWriter out, Date value) {
        final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        out.write("TO_TIMESTAMP('");
        out.write(formatter.format(value));
        out.write("', 'DD.MM.YYYY HH24:MI:SS')");
    }
}
