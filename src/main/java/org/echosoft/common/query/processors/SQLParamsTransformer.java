package org.echosoft.common.query.processors;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.echosoft.common.query.Query;

/**
 * This class is responsible for transforming named parameters to anonymous for specified {@link Query} instance
 * according to rules accepted for SQL query language.
 * @author Anton Sharapov
 */
public class SQLParamsTransformer {

    private static final SQLParamsTransformer instance = new SQLParamsTransformer();
    public static SQLParamsTransformer getInstance() {
        return instance;
    }
    private SQLParamsTransformer() {
    }


    /**
     * <p>Transforms named parameters in passed query constraints to anonymous and
     * makes corresponding modifications in the text if SQL query.</p> 
     * <code>query</code> argument before transformation may contains anonymous and/or named parameters. After
     * transformation this argument will contains only anonymous parameters.
     * @param sql  text of the base SQL
     * @param query  query constraints which should be applied for given sql query.
     */
    public void transform(StringBuffer sql, Query query) {
        if (query==null || query.getNamedParams().isEmpty())
            return;

        final TreeMap<Integer,Object> out = new TreeMap<Integer,Object>();
        processAnonymousParams(sql, query.getParams(), out);
        for (Iterator<Map.Entry<String,Object>> entries = query.getNamedParams().entrySet().iterator(); entries.hasNext(); ) {
            final Map.Entry<String,Object> entry = entries.next();
            processNamedParam(sql, entry.getKey(), entry.getValue(), out);
            entries.remove();
        }
        if (!out.isEmpty()) {
            final Object params[] = new Object[ (out.lastKey())+1 ];
            for (Map.Entry<Integer,Object> entry : out.entrySet()) {
                final int pos = entry.getKey();
                params[pos] = entry.getValue();
            }
            query.setParams(params);
        } else {
            query.setParams(null);
        }
    }


    /**
     * Changes order of initially existed anonymous parameters.
     * @param SQL  contains text of the sql query
     * @param params  an array of the anonymous parameters before transformation
     * @param out  contains ordered map of all parameters (key - parameter's order in list, value - parameter's value)
     */
    protected void processAnonymousParams(final StringBuffer SQL, Object params[], TreeMap<Integer,Object> out) {
        int startPos = 0;
        int startParamNumber = 0;
        for (int i=0; i<params.length; i++) {
            int pos1 = SQL.indexOf(":", startPos);
            int pos2 = SQL.indexOf("?", startPos);
            int paramNumber = startParamNumber;
            while (pos2>=0) {
                if (pos1<0 || pos1>pos2) {  // nearest parameter is anonymous
                    out.put(paramNumber, params[i]);
                    startPos = pos2 + 1;
                    startParamNumber = paramNumber + 1;
                    break;
                } else {                    // nearest parameter is named, skip it now
                    pos2 = pos1;
                    paramNumber++;
                    pos2++;
                    pos1 = SQL.indexOf(":", pos2);
                    pos2 = SQL.indexOf("?", pos2);
                }
            }
        }
    }

    /**
     * Transform selected named parameter to anonymous.
     * @param SQL  contains text of the sql query
     * @param param  parameter's name
     * @param value  parameter's value
     * @param out  contains ordered map of all parameters (key - parameter's order in list, value - parameter's value)
     */
    protected void processNamedParam(final StringBuffer SQL, String param, Object value, TreeMap<Integer,Object> out) {
        if (!param.startsWith(":"))
            param = ":"+param;
        int pos1 = SQL.indexOf(":");
        int pos2 = SQL.indexOf("?");
        int paramNumber = 0;
        while (pos1 >= 0) {
            if (pos2<0 || pos2>pos1) {    // nearest parameter is named, check it's name...
                if (SQL.indexOf(param,pos1)==pos1) {
                    SQL.replace(pos1,pos1+param.length(), "?");
                    out.put(paramNumber, value);
                }
            } else {                      // nearest parameter is anonymous, skip it now
                pos1 = pos2;
            }
            paramNumber++;
            pos1++;
            pos2 = SQL.indexOf("?", pos1);
            pos1 = SQL.indexOf(":", pos1);
        }
    }

}
