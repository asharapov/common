package org.echosoft.common.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains resulting data from the any {@link QueryProcessor}.
 *
 * @author Anton Sharapov
 */
public final class QueryResult<T> {

    private final List<T> beans;
    private final BeanMetaData metadata;
    private final int totalSize;
    private final int rangeStart;
    private final int rangeSize;

    public QueryResult(List<T> beans, BeanMetaData metadata, int totalSize, int rangeStart, int rangeSize) {
        this.beans = beans!=null ? beans : Collections.<T>emptyList();
        if (metadata==null) {
            metadata = BeanMetaData.resolveMetaData( beans.size()>0 ? beans.get(0) : null );
        }
        this.metadata = metadata;
        this.totalSize = totalSize > 0 ? totalSize : 0;
        this.rangeStart = rangeStart > 0 ? rangeStart : 0;
        this.rangeSize = rangeSize > 0 ? rangeSize : 0;
    }

    public QueryResult(List<T> beans) {
        this.beans = beans!=null ? beans : Collections.<T>emptyList();
        this.metadata = BeanMetaData.resolveMetaData( beans.size()>0 ? beans.get(0) : null );
        this.totalSize = beans.size();
        this.rangeStart = 0;
        this.rangeSize = beans.size();
    }

    public QueryResult(BeanIterator<T> iter) throws Exception {
        if (iter==null)
            throw new IllegalArgumentException();
        this.beans = new ArrayList<T>();
        while (iter.hasNext()) {
            beans.add( iter.next() );
        }
        this.metadata = iter.getMetaData();
        this.totalSize = beans.size();
        this.rangeStart = 0;
        this.rangeSize = beans.size();
    }

    public List<T> getBeans() {
        return beans;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public int getRangeStart() {
        return rangeStart;
    }

    public int getRangeSize() {
        return rangeSize;
    }

    /**
     * Calculates the page number.
     * @return the number of the current page.
     */
    public int getPageNumber() {
        return  rangeSize!=0 ? (rangeStart/rangeSize) : 0;
    }

    /**
     * Calculates count of the pages in the data set.
     * @return count of the pages.
     */
    public int getPageCount() {
        return rangeSize!=0 ? (totalSize/rangeSize) + ( (totalSize%rangeSize)>0 ? 1 : 0) : 0;
    }

    public BeanMetaData getMetaData() {
        return metadata;
    }


    public String toString() {
        return "[QueryResult{page:"+beans.size()+", total:"+totalSize+"}]";
    }
}
