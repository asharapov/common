package org.echosoft.common.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.echosoft.common.utils.BeanUtil2.getProperty;
import static org.echosoft.common.utils.BeanUtil2.setProperty;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Anton Sharapov
 */
public class BeanUtil2Test {

    private static Company company;

    @BeforeClass
    public static void beforeClass() {
        company = new Company("Company 1", "1001", new Person("Ivanov", 1, new Address("russia", "moscow", 101)));
        company.employee.add(new Person("Petrov", 2, null));
    }

    @Test
    public void getStandardProperties() throws Exception {
        Object value;
        value = getProperty(company, "cid");
        assertEquals("Can't get 'cid' field", company.cid, value);
        value = getProperty(company, "name");
        assertEquals("Can't get 'name' property", company.getName(), value);
        value = getProperty(company, "director");
        assertEquals("Can't get 'director' property", company.getDirector(), value);
        value = getProperty(company, "director.name");
        assertEquals("Can't get 'director.name' property", company.getDirector().getName(), value);
        value = getProperty(company, "director.address.country");
        assertEquals("Can't get 'director.address.country' property", company.getDirector().getAddress().getCountry(), value);
        value = getProperty(company, "director.topRated");
        assertEquals("Can't get 'director.topRated' property", company.getDirector().isTopRated(), value);
    }

    @Test
    public void setStandardProperties() throws Exception {
        setProperty(company, "cid", "newcid");
        assertEquals("Can't set 'cid' field", "newcid", company.cid);
        setProperty(company, "name", "newname");
        assertEquals("Can't set 'name' property", "newname", company.getName());
        setProperty(company, "director.name", "newdirectorname");
        assertEquals("Can't set 'director.name' property", "newdirectorname", company.getDirector().getName());
        setProperty(company, "director.address.country", "Soviet Union");
        assertEquals("Can't set 'director.address.country' property", "Soviet Union", company.getDirector().getAddress().getCountry());
        setProperty(company, "director.rate", 13);
        assertEquals("Can't set 'director.rate' property", 13, company.getDirector().getRate());
    }

    @Test
    public void getIndexedProperties() throws Exception {
        Object value;
        value = getProperty(company, "director.awards[2]");
        assertEquals("Can't get indexed property 'director.awards[2]'", company.getDirector().getAwards()[2], value);
        value = getProperty(company, "director.awards[1].bytes");
        assertArrayEquals("Can't get indexed property 'director.awards[1].bytes'", company.getDirector().getAwards()[1].getBytes(), (byte[]) value);
        value = getProperty(company, "director.skills[0]");
        assertEquals("Can't get indexed property 'director.skills[0]'", company.getDirector().getSkills().get(0), value);
    }

    @Test
    public void setIndexedProperties() throws Exception {
        setProperty(company, "director.awards[2]", "222");
        assertEquals("Can't set 'director.awards[2]' property", "222", company.getDirector().getAwards()[2]);
        setProperty(company, "director.skills[2]", "LL3");
        assertEquals("Can't set 'director.skills[2]' property", "LL3", company.getDirector().getSkills().get(2));
    }

    @Test
    public void getMappedValues() throws Exception {
        Object value;
        value = getProperty(company, "director.env.k1");
        assertEquals("Can't get 'director.env.k1' map entry", company.getDirector().getEnv().get("k1"), value);
        value = getProperty(company, "director.getEnv.k2");
        assertEquals("Can't get 'director.env.k1' map entry", company.getDirector().getEnv().get("k2"), value);
//        value = getProperty(company, "director.someone.keystr");
//        assertEquals("Can't get 'director.someone.keystr' map entry", company.getDirector().getSomeone("keystr"), value);
//        value = getProperty(company, "director.getSomeone.keystr");
//        assertEquals("Can't get 'director.getSomeone.keystr' map entry", company.getDirector().getSomeone("keystr"), value);
    }

    @Test
    public void setMappedValues() throws Exception {
        setProperty(company, "director.env.p1", "pv1");
        assertEquals("Can't set 'director.env.p1' map entry", "pv1", company.getDirector().getEnv().get("p1"));
//        setProperty(company, "director.someone.k3", "v3");
//        assertEquals("Can't set 'director.someone.k3' map entry", "v3", company.getDirector().getSomeone("k3"));
//        setProperty(company, "director.getSomeone.k4", "v4");
//        assertEquals("Can't set 'director.getSomeone.k4' map entry", "v4", company.getDirector().getSomeone("k4"));
    }

    @Test
    public void getMethodsWithNoArgs() throws Exception {
        Object value;
        value = getProperty(company, "hasChildren");
        assertEquals("Can't call 'Company.hasChildren()' method", company.hasChildren(), value);
        value = getProperty(company, "director.address.hashCode");
        assertEquals("Can't call 'Address.hashCode()' method", company.getDirector().getAddress().hashCode(), value);
    }

    @Test
    public void getMethodsWithOneArg() throws Exception {
        Object value;
        value = getProperty(company, "director.getSomeone(keystr)");
        assertEquals("Can't call 'Person.getSomeone(keystr)' method", company.getDirector().getSomeone("keystr"), value);
        value = getProperty(company, "director.someone(keystr)");
        assertEquals("Can't call 'Person.getSomeone(keystr)' method", company.getDirector().getSomeone("keystr"), value);
        value = getProperty(company, "director.getMethod1(1)");
        assertEquals("Can't call 'Director.getMethod1(1)' method", company.getDirector().getMethod1(1), value);
        value = getProperty(company, "director.method1(2)");
        assertEquals("Can't call 'Director.getMethod1(2)' method", company.getDirector().getMethod1(2), value);
        value = getProperty(company, "director.getMethod2(1)");
        assertEquals("Can't call 'Director.getMethod2(1)' method", company.getDirector().getMethod2(1), value);
        value = getProperty(company, "director.method2(2)");
        assertEquals("Can't call 'Director.getMethod2(2)' method", company.getDirector().getMethod2(2), value);
        value = getProperty(company, "top(-1)");
        assertEquals("Can't call 'Company.top(-1))' method", company.getTop(-1), value);
        value = getProperty(company, "bottom(-2)");
        assertEquals("Can't call 'Company.bottom(-2)' method", company.bottom(-2), value);
    }


    public static final class Company {
        public String cid;
        private String name;
        private String inn;
        private Person director;
        public final List<Person> employee;
        public Company(String name, String inn, Person director) {
            this.name = name;
            this.inn = inn;
            this.director = director;
            this.cid = "fucked field";
            this.employee = new ArrayList<Person>();
            employee.add( director );
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getInn() {
            return inn;
        }
        public void setInn(String inn) {
            this.inn = inn;
        }
        public Person getDirector() {
            return director;
        }
        public void setDirector(Person director) {
            this.director = director;
        }
        public boolean hasChildren() {
            return name != null;
        }
        public String getTop(int arg) {
            return "[" + arg + "]";
        }
        public String bottom(int arg) {
            return "<" + arg + ">";
        }
    }

    public static final class Person {
        private String name;
        private int rate;
        private Address address;
        private Map<Object,Object> env;
        private List<String> skills;
        private String awards[];
        public Person(String name, int rate, Address address) {
            this.name = name;
            this.rate = rate;
            this.address = address;
            this.env = new HashMap<Object,Object>();
            this.env.put("keystr", "val:str");
            this.env.put(1, "val:1");
            this.env.put("k1", 1);
            this.env.put("k2", 2);
            this.skills = new ArrayList<String>();
            this.skills.add("L1");
            this.skills.add("L2");
            this.skills.add("L3");
            this.awards = new String[]{"A1", "A2", "A3", "A4", "A5"};
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public int getRate() {
            return rate;
        }
        public void setRate(int rate) {
            this.rate = rate;
        }
        public Address getAddress() {
            return address;
        }
        public void setAddress(Address address) {
            this.address = address;
        }
        public boolean isTopRated() {
            return rate>0;
        }
        public Map getEnv() {
            return env;
        }
        public List getSkills() {
            return skills;
        }
        public String[] getAwards() {
            return awards;
        }
        public Object getSomeone(String param) {
            return env.get(param);
        }
        public void setSomeone(String param, Object value) {
            env.put(param, value);
        }
        public Integer getMethod1(Integer param) {
            return param + 1;
        }
        public void setMethod1(Integer param, Object value) {
            System.out.println("called setMethod1(" + param + ", " + value + ") ...");
        }
        public int getMethod2(int param) {
            return param + 1;
        }
    }

    public static final class Address implements Serializable {
        private String country;
        private String city;
        private int home;
        public Address() {
        }
        public Address(String country, String city, int home) {
            this.country = country;
            this.city = city;
            this.home = home;
        }
        public String getCountry() {
            return country;
        }
        public void setCountry(String country) {
            this.country = country;
        }
        public String getCity() {
            return city;
        }
        public void setCity(String city) {
            this.city = city;
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
        public boolean equals(final Object obj) {
            if (obj == null || !getClass().equals(obj.getClass()))
                return false;
            final Address other = (Address) obj;
            return (country != null ? country.equals(other.country) : other.country == null) &&
                    (city != null ? city.equals(other.city) : other.city == null) &&
                    home == other.home;
        }
    }
}
