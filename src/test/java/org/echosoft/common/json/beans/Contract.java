package org.echosoft.common.json.beans;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anton Sharapov
 */
public class Contract {

    public enum State{ DRAFT, APPROVED, EXECUTED, PAYED }

    public int number;
    public Date date;
    public State state;
    public List<Item> items;
    public Map<String,Object> params;

    protected transient BigDecimal totalCost;


    public Contract(int number, Date date, State state) {
        this.number = number;
        this.date = date;
        this.state = state;
        this.items = new ArrayList<Item>();
        this.params = new HashMap<String,Object>();
    }

    public BigDecimal getTotalCost() {
        BigDecimal result = BigDecimal.ZERO;
        for (Item item : items) {
            result = result.add(item.getCost());
        }
        totalCost = result;
        return result;
    }

    public int hashCode() {
        return number;
    }
    public boolean equals(Object obj) {
        if (obj==null || !getClass().equals(obj.getClass()))
            return false;
        final Contract other = (Contract)obj;
        return number==other.number && 
               (date!=null ? date.equals(other.date) : other.date==null) &&
               state==other.state &&  items.equals(other.items);
    }
    public String toString() {
        final SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy");
        return "[Contract{number:"+number+", date:"+(date!=null ? fmt.format(date) : "null")+", state:"+state+", items:"+items.size()+", cost:"+getTotalCost()+"}]";
    }
}