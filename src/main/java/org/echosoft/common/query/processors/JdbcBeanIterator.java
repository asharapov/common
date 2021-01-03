package org.echosoft.common.query.processors;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.NoSuchElementException;

import org.echosoft.common.query.BeanIterator;
import org.echosoft.common.query.BeanLoader;
import org.echosoft.common.query.BeanMetaData;

/**
 * @author Anton Sharapov
 */
public final class JdbcBeanIterator<T> implements BeanIterator<T> {

    private final Connection conn;
    private final Statement stmt;
    private final ResultSet rs;
    private final BeanLoader<T> loader;
    private final int maxCount;
    private BeanMetaData metadata;
    private boolean hasNextBean;
    private T nextBean;
    private int processed;

    public JdbcBeanIterator(Connection conn, Statement stmt, ResultSet rs, BeanLoader<T> loader) throws Exception {
        this(conn, stmt, rs, loader, 0, 0);
    }

    public JdbcBeanIterator(Connection conn, Statement stmt, ResultSet rs, BeanLoader<T> loader, int rangeStart, int rangeSize) throws Exception {
        if (conn==null || stmt==null || rs==null || loader==null)
            throw new IllegalArgumentException("All arguments must be specified");
        this.conn = conn;
        this.stmt = stmt;
        this.rs = rs;
        this.loader = loader;
        this.maxCount = rangeSize;
        if (rangeStart > 0) {
            try {
                rs.absolute(rangeStart);
            } catch (Exception e) {
                for (int i=0; i<rangeStart; i++)  rs.next();
            }
        }
        checkNextBean();
    }

    /**
     * {@inheritDoc}
     */
    public BeanMetaData getMetaData() {
        if (metadata==null) {
            metadata = new BeanMetaData( loader.getMetadata() );
        }
        return metadata;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        return hasNextBean;
    }

    /**
     * {@inheritDoc}
     */
    public T next() throws Exception {
        if (!hasNextBean)
            throw new NoSuchElementException();
        final T result = nextBean;
        checkNextBean();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public T readAhead() {
        if (!hasNextBean)
            throw new NoSuchElementException();
        return nextBean;
    }


    /**
     * {@inheritDoc}
     */
    public void close() {
        if (rs!=null)
        try {
            rs.close();
        } catch (Throwable th) { th.printStackTrace(System.err); }
        if (stmt!=null)
        try {
            stmt.close();
        } catch (Throwable th) { th.printStackTrace(System.err); }
        if (conn!=null)
        try {
            conn.close();
        } catch (Throwable th) { th.printStackTrace(System.err); }
    }

    private void checkNextBean() throws Exception {
        hasNextBean = rs.next()  && (maxCount<=0 || processed++<maxCount);
        if (hasNextBean) {
            nextBean = loader.load(rs);
        } else {
            nextBean = null;
        }
    }

}
