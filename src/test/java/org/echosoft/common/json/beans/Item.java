package org.echosoft.common.json.beans;

import java.math.BigDecimal;

import org.echosoft.common.json.annotate.JsonField;


/**
 * @author Anton Sharapov
 */
public class Item {

    public String name;
    @JsonField
    public int quantity;
    @JsonField(name = "price")
    public BigDecimal unitCost;

    public Item(String name, int quantity, double unitCost) {
        this.name = name;
        this.quantity = quantity;
        this.unitCost = new BigDecimal(unitCost);
    }

    public BigDecimal getCost() {
        return unitCost!=null ? unitCost.multiply(new BigDecimal(quantity)) : BigDecimal.ZERO;
    }

    @JsonField(isTransient = true)
    public boolean isPremium() {
        return unitCost.compareTo(new BigDecimal(1000))>0;
    }


    public int hashCode() {
        return name!=null ? name.hashCode() : 0;
    }
    public boolean equals(Object obj) {
        if (obj==null || !getClass().equals(obj.getClass()))
            return false;
        final Item other = (Item)obj;
        return (name!=null ? name.equals(other.name) : other.name==null) &&
               (unitCost!=null ? unitCost.equals(other.unitCost) : other.unitCost==null) && quantity==other.quantity;
    }
    public String toString() {
        return "[Item{name:"+name+", quantity:"+quantity+", unitCost:"+unitCost+"}]";
    }
}
