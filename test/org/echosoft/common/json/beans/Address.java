package org.echosoft.common.json.beans;

import java.io.Serializable;

/**
 * @author Anton Sharapov
 */
public class Address implements Serializable {

    private String state;
    private String city;
    private String street;
    private int home;

    public Address(String state, String city, String street, int home) {
        this.state = state;
        this.city = city;
        this.street = street;
        this.home = home;
    }


    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }
    public void setStreet(String street) {
        this.street = street;
    }

    public int getHome() {
        return home;
    }
    public void setHome(int home) {
        this.home = home;
    }


    public int hashCode() {
        return home;
    }
    public boolean equals(Object obj) {
        if (obj==null || !Address.class.equals(obj.getClass()))
            return false;
        final Address other = (Address)obj;
        return (state!=null ? state.equals(other.state) : other.state==null) &&
               (city!=null ? city.equals(other.city) : other.city==null) &&
               (street!=null ? street.equals(other.street) : other.street==null) &&
               home==other.home;
    }
    public String toString() {
        return "[Address{state:"+state+", city:"+city+", street:"+street+", home:"+home+"}]";
    }
}
