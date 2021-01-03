package org.echosoft.common.query.processors;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
import org.echosoft.common.utils.StringUtil;

/**
 * This implementation of the {@link QueryProcessor} adapted for the oracle database.
 * @author Anton Sharapov
 */
public class OracleQueryProcessor implements QueryProcessor {

    public static final int DEFAULT_FETCH_SIZE = 1000;
    private static final Object[] EMPTY_ARRAY = new Object[0];

    private static final OracleQueryProcessor instance = new OracleQueryProcessor();
    public static QueryProcessor getInstance() {
        return OracleQueryProcessor.instance;
    }
    private OracleQueryProcessor() {
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
            params = EMPTY_ARRAY;
        }
        int totalSize = estimatedTotalSize;
        final BeanMetaData metadata;
        final List<T> beans = new ArrayList<T>(rangeSize>0 ? Math.min(rangeSize,DEFAULT_FETCH_SIZE) : DEFAULT_FETCH_SIZE);

        final Connection conn = ds.getConnection();
        try {
            final String pageSQL = applyQuery(baseSQL, query, loader);
            final PreparedStatement pstmt1 = conn.prepareStatement(pageSQL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            try {
                pstmt1.setFetchDirection(ResultSet.FETCH_FORWARD);
                pstmt1.setFetchSize(rangeSize>0 ? Math.min(rangeSize,DEFAULT_FETCH_SIZE) : DEFAULT_FETCH_SIZE);
                for (int i=0; i<params.length; i++) {
                    if (params[i] instanceof Date) {
                        pstmt1.setTimestamp(i+1, new Timestamp(((Date)params[i]).getTime()));
                    } else {
                        pstmt1.setObject(i+1, params[i]);
                    }
                }
                final ResultSet rs = pstmt1.executeQuery();
                try {
                    if (loader instanceof JdbcBeanLoader) {
                        ((JdbcBeanLoader)loader).setMetaData(rs.getMetaData());
                    }
                    metadata = new BeanMetaData( loader.getMetadata() );

                    while (rs.next()) {
                        if (totalSize<=0 && rangeSize>0) {
                            totalSize = rs.getInt("SYS$CNT");
                        }
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

            if (rangeSize>0 && totalSize<=0) {
                final String totalCountSQL = getTotalRowCountQuery(baseSQL, query, loader);
                final PreparedStatement pstmt2 = conn.prepareStatement(totalCountSQL);
                try {
                    for (int i=0; i<params.length; i++) {
                        if (params[i] instanceof Date) {
                            pstmt2.setTimestamp(i+1, new Timestamp(((Date)params[i]).getTime()));
                        } else {
                            pstmt2.setObject(i+1, params[i]);
                        }
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
            } else
            if (totalSize<=0) {
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
        final int rangeSize = query!=null && query.getPageSize()>0 ? query.getPageSize() : 0;
        final Object params[] = query!=null ? query.getParams() : new Object[0];

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
                if (params[i] instanceof Date) {
                    pstmt.setTimestamp(i+1, new Timestamp(((Date)params[i]).getTime()));
                } else {
                    pstmt.setObject(i+1, params[i]);
                }
            }
            rs = pstmt.executeQuery();
            if (loader instanceof JdbcBeanLoader) {
                ((JdbcBeanLoader)loader).setMetaData(rs.getMetaData());
            }

            return new JdbcBeanIterator<T>(conn, pstmt, rs, loader);

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
    public String applyQuery(final String baseSQL, final Query query, final BeanLoader loader) {
        if (query==null || (!query.hasConstraints() && query.getSortCriteria().isEmpty() && query.getPageSize()<=0))
            return baseSQL;

        final int size = query.getPageSize()>0 ? query.getPageSize() : 0;
        final int from = query.getPageNumber()*size;

        // generate sql expression without paging support ...
        final FastStringWriter filtered = new FastStringWriter(50+baseSQL.length());
        filtered.write("SELECT ");
        if (size>0 && query.getEstimatedTotalSize()<=0)
            filtered.write("count(*) over () sys$cnt, ");
        filtered.write("q.* FROM (\n");
        filtered.write(baseSQL);
        filtered.write("\n) q\n");
        applyWhereClause(filtered, query, loader);
        applyOrderClause(filtered, query, loader);

        if (size>0) {
            // add paging support ...
            final FastStringWriter out = new FastStringWriter(64+filtered.length());
//            if (from==0) {
//                out.write("SELECT * FROM (\n");
//                filtered.writeOut(out);
//                out.write("\n)\nWHERE rownum <= ");
//                out.write( Integer.toString(size) );
//            } else {
                out.write("SELECT * FROM (\nSELECT qq.*, rownum as qry_rownum FROM (\n");
                filtered.writeOut(out);
                out.write("\n) qq\n)\nWHERE qry_rownum BETWEEN ");
                out.write(Integer.toString(from+1));
                out.write(" AND ");
                out.write(Integer.toString(from+size));
//            }
            return out.toString();
        } else {
            return filtered.toString();
        }
    }


    protected String getTotalRowCountQuery(final String baseSQL, final Query query, final BeanLoader loader) throws SQLException {
        final FastStringWriter out = new FastStringWriter(32+baseSQL.length());
        out.write("SELECT count(*) FROM (\n");
        out.write(baseSQL);
        out.write(") qry \n");
        applyWhereClause(out, query, loader);
        return out.toString();
    }


    protected void applyWhereClause(final FastStringWriter out, final Query query, final BeanLoader loader) {
        if (query==null || !query.hasConstraints())
            return;
        out.write("WHERE ");
        for (Iterator i = query.constraints(); i.hasNext(); ) {
            final QueryConstraint constraint = (QueryConstraint)i.next();
            final String field = loader!=null ? loader.getMappedField(constraint.getAttrName()) : constraint.getAttrName();
            if (field==null)
                throw new IllegalArgumentException("No mapped field for attribute "+constraint.getAttrName());
            applyConstraint(out, constraint, field);
            if (i.hasNext())
                out.write("\nAND ");
        }
        out.write('\n');
    }

    protected void applyConstraint(final FastStringWriter out, final QueryConstraint constraint, final String fieldName) {
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
                out.write(operator.getName());
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

    protected void applyOrderClause(final FastStringWriter out, final Query query, final BeanLoader loader) {
        if (query.getSortCriteria().isEmpty())
            return;
        out.write("ORDER BY ");
        final SortCriterion order[] = query.getSortCriteria().toArray();
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



    protected void encodeValue(final FastStringWriter out, final Object value, final boolean upcase) {
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

    protected void encodeCollection(final FastStringWriter out, final Collection values, final boolean upcase) {
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

    protected void encodeCollection(final FastStringWriter out, final Object values[], final boolean upcase) {
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

    protected void encodeString(final FastStringWriter out, final String value, final boolean upcase) {
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

    protected void encodeDate(final FastStringWriter out, final Date value) {
        out.write("TO_DATE('");
        out.write( StringUtil.formatDate(value) );
        out.write("', 'DD.MM.YYYY')");
    }

    protected void encodeDateTime(final FastStringWriter out, final Date value) {
        out.write("TO_DATE('");
        out.write( StringUtil.formatDateTime(value) );
        out.write("', 'DD.MM.YYYY HH24:MI:SS')");
    }

    /*
    private Data prepareQuery(final String baseSQL, final Query query, final BeanLoader loader) {
        final Data data = new Data(baseSQL, query, loader);

        if (query==null || (!query.hasConstraints() && query.getSortCriteria().isEmpty() && query.getPageSize()<=0)) {
            data.out.write(baseSQL);
            return data;
        }

        final int size = query.getPageSize()>0 ? query.getPageSize() : 0;
        final int from = query.getPageNumber()*size;

        // generate sql expression without paging support ...
//        final FastStringWriter filtered = new FastStringWriter(50+baseSQL.length());
        data.out.write("SELECT ");
        if (size>0 && query.getEstimatedTotalSize()<=0)
            data.out.write("count(*) over () sys$cnt, ");
        data.out.write("q.* FROM (\n");
        data.out.write(baseSQL);
        data.out.write("\n) q\n");
        applyWhereClause(data);
        applyOrderClause(data.out, query, loader);

        if (size>0) {
            // add paging support ...
            final FastStringWriter out = new FastStringWriter(64+data.out.length());
            if (from==0) {
                out.write("SELECT * FROM (\n");
                data.out.writeOut(out);
                out.write("\n)\nWHERE rownum <= ");
                out.write( Integer.toString(size) );
            } else {
                out.write("SELECT * FROM (\nSELECT qry.*, rownum as qry_rownum FROM (\n");
                data.out.writeOut(out);
                out.write("\n) qry\n)\nWHERE qry_rownum BETWEEN ");
                out.write(Integer.toString(from+1));
                out.write(" AND ");
                out.write(Integer.toString(from+size));
            }
            data.out = out;
        } else {
        }
        return data;
    }

    private void applyWhereClause(final Data data) {
        if (!data.query.hasConstraints())
            return;
        data.out.write("WHERE ");
        for (Iterator i = data.query.constraints(); i.hasNext(); ) {
            final QueryConstraint constraint = (QueryConstraint)i.next();
            final String field = data.loader!=null ? data.loader.getMappedField(constraint.getAttrName()) : constraint.getAttrName();
            if (field==null)
                throw new IllegalArgumentException("No mapped field for attribute "+constraint.getAttrName());
            applyConstraint(data.out, constraint, field);
            if (i.hasNext())
                data.out.write("\nAND ");
        }
        data.out.write('\n');
    }

    private static final class Data {
        public final String baseSQL;
        public final Query query;
        public final BeanLoader loader;
        public final ArrayList<Object> params;
        public FastStringWriter out;
        public Data(String baseSQL, Query query, BeanLoader loader) {
            this.baseSQL = baseSQL;
            this.query = query;
            this.loader = loader;
            this.params = new ArrayList<Object>();
            if (query!=null) {
                this.params.addAll( Arrays.asList(query.getParams()) );
            }
            out = new FastStringWriter(Math.min(baseSQL.length(), 128));
        }
    }
    */
}
