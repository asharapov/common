package org.echosoft.common.data.db;

import java.util.List;

/**
 * Описывает результат постраничной выборки данных.
 *
 * @author Anton Sharapov
 */
public class PagedQueryResult<T> {

    private final List<T> beans;
    private final long totalSize;
    private final long rangeStart;
    private final int rangeSize;

    public PagedQueryResult(final List<T> beans, final long totalSize, final long rangeStart, final int rangeSize) {
        this.beans = beans;
        this.totalSize = totalSize;
        this.rangeStart = rangeStart;
        this.rangeSize = rangeSize;
    }

    public PagedQueryResult(final List<T> beans) {
        this.beans = beans;
        this.totalSize = beans.size();
        this.rangeStart = 1;
        this.rangeSize = beans.size();
    }

    public List<T> getBeans() {
        return beans;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public long getRangeStart() {
        return rangeStart;
    }

    public int getRangeSize() {
        return rangeSize;
    }

    public long getPage() {
        return rangeSize > 0 && rangeStart > 0 ? (rangeStart - 1) / rangeSize + 1 : 0;
    }

    public long getPagesCount() {
        return rangeSize > 0 && totalSize > 0 ? (totalSize - 1) / rangeSize + 1 : 0;
    }

    @Override
    public String toString() {
        return "[PagedQueryResult{total:" + totalSize + ", start:" + rangeStart + ", size:" + rangeSize + ", beans cnt:" + beans.size() + "}]";
    }
}
