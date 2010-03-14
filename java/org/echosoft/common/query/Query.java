package org.echosoft.common.query;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.codec.binary.Base64;


/**
 * Allows to specify something additional formal rules for quering various data from miscelaneous storages. <br/>
 * Supported the next kinds of rules:
 * <ul>
 * <li>Constraints for querying data. Theirs applied on possible values of attributes of requested entities.
 * <li>Parameters for querying data. Supported both types of parameters: anonymous and named.
 * <li>Ordering rules.
 * <li>Paging support.
 * </ul>
 * All rules gathered in this objects can be processed by various {@link QueryProcessor} implementations.
 * @see QueryConstraint
 * @see QueryOperator
 * @see SortCriteria
 * @see SortCriterion
 * @see QueryProcessor
 * @author Anton Sharapov
 */
public final class Query implements Serializable, Cloneable {

    private final static Object[] EMPTY_PARAMS = new Object[0];

    /**
     * Serialize given query instance to MIME encoded string
     * @param query  query instance which will be encoded to string
     * @return serialized form of query
     */
    public static String serialize(Query query) {
        return serialize(query, false);
    }

    public static String serialize(Query query, boolean archive) {
        if (query==null)
            return null;
        try {
            final ByteArrayOutputStream bout = new ByteArrayOutputStream(1024);
            final OutputStream xout = archive ? new GZIPOutputStream(bout) : bout;
            final ObjectOutput out = new ObjectOutputStream(xout);
            write(query, out);
            out.close();
            final byte[] data = Base64.encodeBase64(bout.toByteArray());
            final String str = new String(data, "ISO-8859-1");
            return archive ? "!"+str : str;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    public static Query deserialize(String encodedData) {
        if (encodedData==null || encodedData.length()==0)
            return null;
        final boolean archive = encodedData.charAt(0)=='!';
        final byte[] data;
        try {
            data = archive ? encodedData.substring(1).getBytes("ISO-8859-1") : encodedData.getBytes("ISO-8859-1");
            final ByteArrayInputStream bin = new ByteArrayInputStream(Base64.decodeBase64(data));
            final InputStream xin = archive ? new GZIPInputStream(bin) : bin;
            final ObjectInputStream in = new ObjectInputStream(xin);
            final Query result = read(in);
            in.close();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void write(final Query query, final ObjectOutput out) throws IOException {
        out.writeInt(query.pageNo);
        out.writeInt(query.pageSize);
        out.writeInt(query.estimatedTotalSize);
        out.writeInt(query.params.length);
        for (Object param : query.params) {
            out.writeObject(param);
        }
        out.writeInt(query.namedParams.size());
        for (Map.Entry<String,Object> entry : query.namedParams.entrySet()) {
            out.writeUTF(entry.getKey());
            out.writeObject(entry.getValue());
        }
        final SortCriterion[] criteria = query.sortCriteria.toArray();
        out.writeInt(criteria.length);
        for (SortCriterion criterion : criteria) {
            out.writeUTF(criterion.getField());
            out.writeBoolean(criterion.isAscending());
        }
        out.writeInt(query.constraints.size());
        for (QueryConstraint constraint : query.constraints) {
            out.writeUTF(constraint.getAttrName());
            out.writeUTF(constraint.getOperator().getName());
            out.writeObject(constraint.getValue());
            out.writeBoolean(constraint.isNegative());
            out.writeBoolean(constraint.isCaseSensitive());
        }
    }

    public static Query read(final ObjectInput in) throws IOException, ClassNotFoundException {
        final Query query = new Query();
        query.pageNo = in.readInt();
        query.pageSize = in.readInt();
        query.estimatedTotalSize = in.readInt();
        int length = in.readInt();
        query.params = new Object[length];
        for (int i=0; i<length; i++) {
            query.params[i] = in.readObject();
        }
        length = in.readInt();
        for (int i=0; i<length; i++) {
            final String name = in.readUTF();
            final Object value = in.readObject();
            query.namedParams.put(name, value);
        }
        length = in.readInt();
        for (int i=0; i<length; i++) {
            final String attr = in.readUTF();
            final boolean ascending = in.readBoolean();
            query.sortCriteria.append(attr, ascending);
        }
        length = in.readInt();
        for (int i=0; i<length; i++) {
            final String attr = in.readUTF();
            final String opname = in.readUTF();
            final Object value = in.readObject();
            final boolean negative = in.readBoolean();
            final boolean cs = in.readBoolean();
            query.constraints.add( new QueryConstraint(attr, QueryOperator.getInstance(opname), value, negative, cs) );
        }
        return query;
    }



    private final List<QueryConstraint> constraints;
    private final SortCriteria sortCriteria;
    private final Map<String,Object> namedParams;
    private Object params[];
    private int pageNo;
    private int pageSize;
    private int estimatedTotalSize;
    private transient int hashcode;

    /**
     * Constructs new Query instance with no constraints.
     * No paging constraints setted by default.
     */
    public Query() {
        this(0, 0);
    }

    /**
     * Constructs new Query instance with specified page number and size
     * @param pageNo  current page number
     * @param pageSize  page size. If size lesser than 1 then no paging will be applied.
     */
    public Query(int pageNo, int pageSize) {
        this.constraints = new ArrayList<QueryConstraint>(2);
        this.sortCriteria = new SortCriteria();
        this.params = EMPTY_PARAMS;
        this.namedParams = new HashMap<String,Object>(2);
        this.pageNo = pageNo<0 ? 0 : pageNo;
        this.pageSize = pageSize<0 ? 0 : pageSize;
        this.estimatedTotalSize = 0;
    }

    /**
     * Returns number of page of the requested data which should be retrieved from storage
     * @return number of requested data page
     */
    public int getPageNumber() {
        return pageNo;
    }
    public void setPageNumber(int pageNo) {
        this.pageNo = pageNo<0 ? 0 : pageNo;
    }

    /**
     * Returns size of page of the requested data which should be retrieved from storage.
     * If page size lesser or equal than 0 then paging will not be used.
     * @return  page size
     */
    public int getPageSize() {
        return pageSize;
    }
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize<0 ? 0 : pageSize;
    }

    /**
     * EstimatedTotalSize property may be used in very saldom cases when requested data readed fully, page by page.
     * In such cases, this property allows us to avoid such redundant operation as recalculating total records count
     * everytime on reading next pages.<br/>
     * If you don't know that is it - don't use this property.
     * @return estimated total rowset size. If this property lesser or equal than <code>0</code> then total size of
     * resultset will be recalculated on each requested page.
     */
    public int getEstimatedTotalSize() {
        return estimatedTotalSize;
    }
    public void setEstimatedTotalSize(int estimatedTotalSize) {
        this.estimatedTotalSize = estimatedTotalSize;
    }


    /**
     * Returns {@link SortCriteria} instance which contains all applied ordering rules.
     * @return corresponding collection of the ordering rules
     */
    public SortCriteria getSortCriteria() {
        return sortCriteria;
    }


    /**
     * Returns <code>true</code> if given query has anonymous parameters.
     * @return <code>true</code> if query has anonymous parameters.
     */
    public boolean hasParams() {
        return params.length>0;
    }

    /**
     * Returns an array of all registered anonymous parameters
     * @return an array of anonymous parameters
     */
    public Object[] getParams() {
        return params;
    }

    /**
     * Set anonymous parameters for given query
     * @param params  an array of parameters values.
     */
    public void setParams(Object[] params) {
        this.params = params == null ? EMPTY_PARAMS : params;
    }

    /**
     * Append new anonymous parameter to query to end of parameters list.
     * @param param  new anonymous parameter.
     */
    public void addParam(Object param) {
        final Object newparams[] = new Object[params.length+1];
        System.arraycopy(params, 0, newparams, 0, params.length);
        newparams[params.length] = param;
        params = newparams;
    }

    /**
     * Retrieves a map of all registered named parameters.
     * Named parameters has limited usage and in most cases will be converted to anonymous before applying this query
     * to requested dataset.
     * @return map of named params.
     */
    public Map<String,Object> getNamedParams() {
        return namedParams;
    }


    /**
     * Returns <code>true</code> if given query has constraints for attributes.
     * @return <code>true</code> if query has attribute constraints.
     */
    public boolean hasConstraints() {
        return constraints.size()>0;
    }

    /**
     * Returns count of registered constraints on entity attributes.
     * @return count of registered constraints.
     */
    public int getConstraintsCount() {
        return constraints.size();
    }

    /**
     * Returns mutable iterator thru all registered constraints on attributes
     * @return mutable iterator of {@link QueryConstraint} instances.
     */
    public Iterator constraints() {
        return constraints.iterator();
    }

    /**
     * Returns mutable iterator thru all registered constraints for the specified attribute
     * @param attrName name of the attribute for which constraints returns.
     * @return mutable iterator of {@link QueryConstraint} instances for specified attribute.
     */
    public Iterator<QueryConstraint> constraints(final String attrName) {
        return new Iterator<QueryConstraint>() {
            int currentPos = -1;
            int nextPos = seekNext(0);
            public boolean hasNext() {
                return nextPos >= 0;
            }
            public QueryConstraint next() {
                if (nextPos < 0)
                    throw new NoSuchElementException();
                currentPos = nextPos;
                nextPos = seekNext(nextPos+1);
                return constraints.get(currentPos);
            }
            public void remove() {
                if (currentPos<0)
                    throw new NoSuchElementException();
                constraints.remove(currentPos);
                nextPos = seekNext(currentPos);
                currentPos = -1;
            }
            int seekNext(int start) {
                for (int i=start; i<constraints.size(); i++) {
                    final QueryConstraint qc = constraints.get(i);
                    if (qc.getAttrName().equals(attrName))
                        return i;
                }
                return -2;
            }
        };
    }

    /**
     * Add new constraint for something attribute of the entity.
     * @param constraint  constraint which will be added.
     */
    public void addConstraint(QueryConstraint constraint) {
        if (!constraints.contains(constraint))
            constraints.add( constraint );
    }

    /**
     * Removes all constraints for the specified attribute
     * @param attrName  name of the attribute
     */
    public void removeConstraintsFor(String attrName) {
        for (int i=constraints.size()-1; i>=0; i--) {
            final QueryConstraint qc = constraints.get(i);
            if (qc.getAttrName().equals(attrName))
                constraints.remove(i);
        }
    }

    /**
     * Removes all registered constraints for any attributes.
     */
    public void clearConstraints() {
        constraints.clear();
    }


    /**
     * Finds constraints for specified attribute and returns value of first of them.
     * @param attrName  attribute name
     * @param defaultValue  method returns this value if no constraints will be finded for given attribute.
     * @return value of first constraint in list (for given attribute) or <code>defaultValue</code> if such constraints was not found.
     */
    public Object getConstraintValue(String attrName, Object defaultValue) {
        final Iterator it = constraints(attrName);
        return it.hasNext() ? ((QueryConstraint)it.next()).getValue() : defaultValue;
    }

    /**
     * This method is combination of two other methods: <code>getConstraintValue(String, Object)</code> and <code>removeConstraintsFor(String)</code>.
     * Method removes all constraints for specified attribute and returns value of the first removed constraint.
     * @param attrName  attribute name
     * @param defaultValue  method returns this value if no constraints will be finded for given attribute.
     * @return value of first constraint from list of removed or <code>defaultValue</code> if such constraints was not found.
     */
    public Object removeConstraintValue(String attrName, Object defaultValue) {
        final Iterator it = constraints(attrName);
        final Object result = it.hasNext() ? ((QueryConstraint)it.next()).getValue() : defaultValue;
        removeConstraintsFor(attrName);
        return result;
    }



    public Object clone() {
        final Query result = new Query(pageNo, pageSize);
        result.setEstimatedTotalSize(estimatedTotalSize);
        result.namedParams.putAll(namedParams);
        result.params = new Object[params.length];
        System.arraycopy(params, 0, result.params, 0, params.length);
        final SortCriterion[] criteria = sortCriteria.toArray();
        for (int i=0; i<criteria.length; i++) {
            final SortCriterion sc = criteria[i];
            result.sortCriteria.append(sc.getField(), sc.isAscending());
        }
        for (int i=0; i<constraints.size(); i++) {
            final QueryConstraint qc = constraints.get(i);
            result.addConstraint( new QueryConstraint(qc.getAttrName(), qc.getOperator(), qc.getValue(), qc.isNegative(), qc.isCaseSensitive()) );
        }
        return result;
    }


    public int hashCode() {
        if (hashcode==0) {
            // we use local variable 'result' to prevent potential problems with concurent calls...
            int result = pageSize + sortCriteria.hashCode();
            for (int i=0; i<constraints.size(); i++) {
                result += constraints.get(i).hashCode() << 4;
            }
            hashcode = result;
        }
        return hashcode;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Query) || hashCode()!=obj.hashCode())
            return false;
        final Query other = (Query)obj;
        return pageNo==other.pageNo && pageSize==other.pageSize &&
               estimatedTotalSize==other.estimatedTotalSize &&
               constraints.equals(other.constraints) &&
               sortCriteria.equals(other.sortCriteria) &&
               Arrays.equals(params, other.params) &&
               namedParams.equals(other.namedParams);
    }

    public String toString() {
        final StringBuilder out = new StringBuilder(255);
        out.append(Integer.toString(pageNo));
        out.append('/');
        out.append(Integer.toString(pageSize));
        if (estimatedTotalSize>0) {
            out.append('/');
            out.append(Integer.toString(estimatedTotalSize));
        }
        if (!sortCriteria.isEmpty()) {
            out.append("  order: ");
            final SortCriterion criterion[] = sortCriteria.toArray();
            for (int i=0; i<criterion.length; i++) {
                if (i>0)
                    out.append(", ");
                out.append(criterion[i].getField());
                out.append( criterion[i].isAscending() ? " ASC" : " DESC");
            }
        }
        if (!constraints.isEmpty()) {
            out.append("\nconstraints:");
            for (QueryConstraint constraint : constraints) {
                out.append("\n  ");
                out.append(constraint.toString());
            }
        }
        if (params.length>0) {
            out.append("\nanonymous params:");
            for (Object param : params) {
                out.append("\n  [");
                out.append(param != null ? param.toString() : "null");
                out.append("] class: ");
                out.append(param != null ? param.getClass().getName() : "null");
            }
        }
        if (!namedParams.isEmpty()) {
            out.append("\nnamed params:");
            out.append(namedParams.toString());
        }

        out.append(']');
        return out.toString();
    }

}
