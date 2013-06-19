package org.echosoft.common.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Базовый класс предназначенный для задания ограничений на выборку данных из некоторого источника.</p>
 * <p>В данном классе реализована возможность сортировки и постраничной выборки данных.</p>
 *
 * @author Anton Sharapov
 */
public class Query implements Serializable {

    private int rangeSize;
    private long rangeStart;
    private Map<String, Object> params;
    private List<SortCriterion> criteria;
    private boolean complete;

    public Query() {
        this.rangeSize = 0;
        this.rangeStart = 0;
    }

    public int getRangeSize() {
        return rangeSize;
    }
    public Query setRangeSize(final int rangeSize) {
        this.rangeSize = rangeSize > 0 ? rangeSize : 0;
        return this;
    }

    public long getRangeStart() {
        return rangeStart;
    }
    public Query setRangeStart(final long rangeStart) {
        this.rangeStart = rangeStart >= 0 ? rangeStart : 0;
        return this;
    }

    public Query selectPage(final long pageNo, final int pageSize) {
        if (pageSize > 0) {
            this.rangeSize = pageSize;
            this.rangeStart = pageNo > 1 ? (pageNo - 1) * pageSize + 1 : 1;
        } else {
            this.rangeSize = 0;
            this.rangeStart = 0;
        }
        return this;
    }


    public boolean hasParams() {
        return params != null && !params.isEmpty();
    }

    public Map<String, Object> getParams() {
        if (params == null)
            params = new HashMap<String, Object>();
        return params;
    }

    public Query addParam(final String paramName, final Object param) {
        if (params == null)
            params = new HashMap<String, Object>();
        params.put(paramName, param);
        return this;
    }


    public Iterable<SortCriterion> getSortCriteria() {
        return criteria != null ? criteria : Collections.<SortCriterion>emptyList();
    }

    public boolean hasSortCriteria() {
        return criteria != null && criteria.size() > 0;
    }

    public Query setSortCriteria(final String field, final boolean ascending) {
        if (criteria == null) {
            criteria = new ArrayList<SortCriterion>(2);
        } else {
            criteria.clear();
        }
        criteria.add(new SortCriterion(field, ascending));
        return this;
    }


    public Query addSortCriterionFirst(final String field, final boolean ascending, final int maxDepth) {
        if (criteria == null) {
            criteria = new ArrayList<SortCriterion>(2);
        } else {
            for (int i = criteria.size(); i >= maxDepth; --i) {
                criteria.remove(i - 1);
            }
            for (int i = criteria.size() - 1; i >= 0; --i) {
                final SortCriterion sc = criteria.get(i);
                if (field.equals(sc.getField())) {
                    criteria.remove(i);
                    break;
                }
            }
        }
        criteria.add(0, new SortCriterion(field, ascending));
        return this;
    }

    public Query addSortCriterionLast(final String field, final boolean ascending) {
        if (criteria == null) {
            criteria = new ArrayList<SortCriterion>(2);
        } else {
            for (int i = criteria.size() - 1; i >= 0; --i) {
                final SortCriterion sc = criteria.get(i);
                if (field.equals(sc.getField())) {
                    criteria.remove(i);
                    break;
                }
            }
        }
        criteria.add(new SortCriterion(field, ascending));
        return this;
    }

    public Query toggleSortCriterion(final String field) {
        if (criteria == null) {
            criteria = new ArrayList<SortCriterion>(2);
        } else {
            for (int i = criteria.size() - 1; i >= 0; --i) {
                final SortCriterion sc = criteria.get(i);
                if (field.equals(sc.getField())) {
                    sc.toggleOrder();
                    return this;
                }
            }
        }
        criteria.add(0, new SortCriterion(field, true));
        return this;
    }


    public boolean isComplete() {
        return complete;
    }
    public void setComplete(final boolean complete) {
        this.complete = complete;
    }
}
