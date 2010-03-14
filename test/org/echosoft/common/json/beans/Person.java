package org.echosoft.common.json.beans;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Anton Sharapov
 */
public class Person {

    private String name;
    private String email;
    private Date bornDate;
    private Address addr;

    public Person(String name, String email, Date bornDate, Address addr) {
        this.name = name;
        this.email = email;
        this.bornDate = bornDate;
        this.addr = addr;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public Date getBornDate() {
        return bornDate;
    }
    public void setBornDate(Date bornDate) {
        this.bornDate = bornDate;
    }

    public Address getAddress() {
        return addr;
    }
    public void setAddress(Address addr) {
        this.addr = addr;
    }


    public int hashCode() {
        return name!=null ? name.hashCode() : 0;
    }
    public boolean equals(Object obj) {
        if (obj==null || !getClass().equals(obj.getClass()))
            return false;
        final Person other = (Person)obj;
        return (name!=null ? name.equals(other.name) : other.name==null) &&
               (email!=null ? email.equals(other.email) : other.email==null) &&
               (bornDate!=null ? bornDate.equals(other.bornDate) : other.bornDate==null) &&
               (addr!=null ? addr.equals(other.addr) : other.addr==null);
    }
    public String toString() {
        final SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yyyy");
        return "[Person{name:"+name+", email:"+email+", born:"+(bornDate!=null ? fmt.format(bornDate) : "null")+", addr:"+addr+"}]";
    }
}
