package org.echosoft.common.db;

import java.util.List;

/**
 * @author Anton Sharapov
 */
public class PagedQueryResult<T> {

    private long totalSize;
    private long rangeStart;
    private int pageSize;
    private List<T> beans;

    public PagedQueryResult(final long totalSize, final List<T> beans) {
        this.totalSize = totalSize;
        this.beans = beans;
    }
}
