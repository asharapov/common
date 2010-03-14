package org.echosoft.common.json.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.echosoft.common.utils.StringUtil;

/**
 * @author Anton Sharapov
 */
public class Company {

    private String name;
    private Date registered;
    private Address addr;
    private Person director;
    private List<Contract> contracts;

    public Company(String name, Date registered, Address addr, Person director) {
        this.name = name;
        this.registered = registered;
        this.addr = addr;
        this.director = director;
        this.contracts = new ArrayList<Contract>();
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }


    public Date getRegistered() {
        return registered;
    }
    public void setRegistered(Date registered) {
        this.registered = registered;
    }

    public Address getAddress() {
        return addr;
    }
    public void setAddress(Address addr) {
        this.addr = addr;
    }

    public Person getDirector() {
        return director;
    }
    public void setDirector(Person director) {
        this.director = director;
    }

    public List<Contract> getContracts() {
        return contracts;
    }


    public boolean hasAddress() {
        return addr!=null;
    }


    public int hashCode() {
        return name!=null ? name.hashCode() : 0;
    }
    public boolean equals(Object obj) {
        if (obj==null || !getClass().equals(obj.getClass()))
            return false;
        final Company other = (Company)obj;
        return (name!=null ? name.equals(other.name) : other.name==null) &&
               (registered!=null ? registered.equals(other.registered) : other.registered==null) &&
               (addr!=null ? addr.equals(other.addr) : other.addr==null) &&
               (director!=null ? director.equals(other.director) : other.director==null);
    }
    public String toString() {
        return "[Company{name:"+name+", registered:"+ StringUtil.formatDate(registered)+", addr:"+addr+", director:"+director+"}]";
    }
}
