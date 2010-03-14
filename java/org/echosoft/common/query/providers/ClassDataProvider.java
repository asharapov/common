package org.echosoft.common.query.providers;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.echosoft.common.query.BeanIterator;
import org.echosoft.common.query.ListBeanIterator;
import org.echosoft.common.query.Query;
import org.echosoft.common.query.QueryResult;
import org.echosoft.common.utils.StringUtil;

/**
 * Implementation of the {@link DataProvider} instance which uses dynamic invocations of the
 * appropriated class methods for querying requested data.
 * The next kinds of methods supported in this implementation:
 * <ul>
 * <li>Methods with one parameter - {@link Query}.
 * <li>Methods without any parameters.
 * </ul>
 * @author Anton Sharapov
 */
public class ClassDataProvider<T> implements DataProvider {

    private final Object object;
    private final Method method;
    private final Class[] paramTypes;

    public ClassDataProvider(Object object, String methodName) {
        if (object==null)
            throw new NullPointerException("Object must be specified");
        if ((methodName= StringUtil.trim(methodName))==null)
            throw new NullPointerException("Method must be specified");
        this.object = object;

        Method method;
        try {
            method = object.getClass().getMethod(methodName, Query.class);
        } catch (NoSuchMethodException e) {
            try {
                method = object.getClass().getMethod(methodName, (Class[])null); // upcasting to Class[] required for compatibility to JSDK 5
            } catch (NoSuchMethodException ee) {
                throw new IllegalArgumentException("Object ["+object+"] has not appropriated method with name ["+methodName+"]");
            }
        }
        this.method = method;
        this.paramTypes = method.getParameterTypes();
    }


    /**
     * Returns queried rows from the data provider.
     * @param query  optional parameter that has additional constraints, sorting rules or paging support for retrieved data.
     * @return lazy iterator through queried dataset.
     * @throws DataProviderException  in case if any errors occurs.
     */
    @SuppressWarnings("unchecked")
    public BeanIterator<T> execute(Query query) throws DataProviderException {
        try {
            final Object result;
            // invoke method ...
            if (paramTypes.length==0) {  // method hasn't any arguments ...
                result = method.invoke(object, (Object[])null); // upcasting to Object[] required for compatibility to JSDK 5
            } else
            if (paramTypes.length==1) {  // method has one argument (should be Query) ...
                result = method.invoke(object, query);
            } else
                throw new IllegalArgumentException("Unsupported method ["+method+"] arguments. ");

            // resolve result values...
            if (result instanceof BeanIterator) {
                return (BeanIterator)result;
            } else
            if (result instanceof QueryResult) {
                return new ListBeanIterator( (QueryResult)result );
            } else
            if (result instanceof List) {
                return new ListDataProvider( (List)result ).execute(query);
            } else
            if (result instanceof Object[]) {
                return new ListDataProvider( (Object[])result ).execute(query);
            } else
            if (result instanceof Iterator) {
                return new ListDataProvider( (Iterator)result ).execute(query);
            } else
            if (result == null) {
                return new ListBeanIterator(null);
            } else
                throw new IllegalArgumentException("Invalid method ["+method+"] return type: "+result.getClass());

        } catch (Exception e) {
            throw new DataProviderException(e.getMessage(), e);
        }
    }


    /**
     * Returns queried rows from the data provider.
     * @param query  optional parameter that has additional constraints, sorting rules or paging support for retrieved data.
     * @return range of the sorted records from the data set.
     * @throws DataProviderException  in case if any errors occurs.
     */
    @SuppressWarnings("unchecked")
    public QueryResult<T> executePaged(final Query query) throws DataProviderException {
        try {
            final Object result;
            // invoke method ...
            if (paramTypes.length==0) {  // method hasn't any arguments ...
                result = method.invoke(object, (Object[])null); // upcasting to Object[] required for compatibility to JSDK 5
            } else
            if (paramTypes.length==1) {  // method has one argument (should be Query) ...
                result = method.invoke(object, query);
            } else
                throw new IllegalArgumentException("Unsupported method ["+method+"] arguments. ");

            // resolve result values...
            if (result instanceof QueryResult) {
                return (QueryResult)result;
            } else
            if (result instanceof BeanIterator) {
                final BeanIterator iter = (BeanIterator)result;
                try {
                    return new QueryResult(iter);
                } finally {
                    iter.close();
                }
            } else
            if (result instanceof List) {
                return new ListDataProvider( (List)result ).executePaged(query);
            } else
            if (result instanceof Object[]) {
                return new ListDataProvider( (Object[])result ).executePaged(query);
            } else
            if (result instanceof Iterator) {
                return new ListDataProvider( (Iterator)result ).executePaged(query);
            } else
            if (result == null) {
                final int pageSize = query!=null ? query.getPageSize() : 0;
                final int pageNum = query!=null ? query.getPageNumber() : 0;
                return new QueryResult(Collections.EMPTY_LIST, null, 0, pageNum*pageSize, pageSize);
            } else
                throw new IllegalArgumentException("Invalid method ["+method+"] return type: "+result.getClass());

        } catch (Exception e) {
            throw new DataProviderException(e.getMessage(), e);
        }
    }

}
