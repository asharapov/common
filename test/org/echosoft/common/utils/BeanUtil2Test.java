package org.echosoft.common.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.echosoft.common.utils.BeanUtil2.getProperty;
import static org.echosoft.common.utils.BeanUtil2.setProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Anton Sharapov
 */
public class BeanUtil2Test {

    private static Company company;

    @BeforeClass
    public static void beforeClass() {
        company = new Company("Company 1", "1001", new Person("Ivanov", 1, new Address("russia", "moscow", 101)));
        company.employee.add( new Person("Petrov", 2, null) );
    }

    @Test
    public void test1() {
    }

    @Test
    public void testSimplePropertiesRead() throws Exception {
        final String companyName = (String)getProperty(company, "name");
        assertEquals("Unable to get simple property", "Company 1", companyName);
        final Person director = (Person)getProperty(company, "director");
        assertEquals("Unable to get custom object property", company.getDirector(), director);
        final String cid = (String)getProperty(company, "cid");
        assertEquals("Unable to get simple public property", company.cid, cid);
        final Boolean hasChildren = (Boolean)getProperty(company, "hasChildren");
        assertEquals("Unable to call simple public method", company.hasChildren(), hasChildren);

        final String directorName = (String)getProperty(company, "director.name");
        assertEquals("Unable to get nested property", company.getDirector().getName(), directorName);
        final String country = (String)getProperty(company, "director.address.country");
        assertEquals("Unable to get nested property", company.getDirector().getAddress().getCountry(), country);
        final int size = (Integer)getProperty(company, "employee.size");
        assertEquals("Unable to get list size", company.employee.size(), size);

        setProperty(company, "name", "New name of company");
        assertEquals("unable to set simple property", "New name of company", company.getName());
        setProperty(company, "director.name", "Petrov");
        assertEquals("unable to set nested property", "Petrov", company.getDirector().getName());
        setProperty(company, "cid", "CID22");
        assertEquals("Unable to set simple public property", "CID22", company.cid);
    }

    @Test
    public void testIndexedPropertiesRead() throws Exception {
        final String award2 = (String)getProperty(company, "director.awards[2]");
        assertEquals("Unable to get indexed property", company.getDirector().getAwards()[2], award2);
        final byte[] bytes = (byte[])getProperty(company, "director.awards[1].bytes");
        assertTrue("Unable to get indexed property", Arrays.equals(company.getDirector().getAwards()[1].getBytes(), bytes));

        setProperty(company.getDirector(), "awards[0]", "A1X");
        assertEquals("Unable to set indexed property", company.getDirector().getAwards()[0], "A1X");
        setProperty(company, "director.skills[2]", "L2X");
        assertEquals("Unable to set indexed property", company.getDirector().getSkills().get(2), "L2X");
    }

    @Test
    public void testMappedProperties() throws Exception {
        final Object obj1 = getProperty(company, "director.env.keystr");
        assertEquals("Unable to get mapped property", company.getDirector().getEnv().get("keystr"), obj1);
        final Object obj2 = getProperty(company, "director.env(keystr)");
        assertEquals("Unable to get mapped property", company.getDirector().getEnv().get("keystr"), obj2);
        final Object obj3 = getProperty(company.getDirector(), "env(keystr)");
        assertEquals("Unable to get mapped property", company.getDirector().getEnv().get("keystr"), obj3);

        // TODO: failed ...
//        setProperty(company, "director.env(a2)", "A2");
//        assertEquals("unable to set mapped property", company.getDirector().getEnv().get("a2"), "A2");
        setProperty(company, "director.env.a3", "A3");
        assertEquals("unable to set mapped property", company.getDirector().getEnv().get("a3"), "A3");
    }

    @Test
    public void testMethodsCall() throws Exception {
        final Object obj1 = getProperty(company, "director.someone(keystr)");
        assertEquals("Unable to call method ", company.getDirector().getSomeone("keystr"), obj1);
        final Object obj2 = getProperty(company, "director.getSomeone(keystr)");
        assertEquals("Unable to call method ", company.getDirector().getSomeone("keystr"), obj2);
        final Object obj3 = getProperty(company.getDirector(), "method1(2)");
        assertEquals("Unable to call method ", company.getDirector().getMethod1(2), obj3);
        final Object obj4 = getProperty(company.getDirector(), "method2(2)");
        assertEquals("Unable to call method ", company.getDirector().getMethod2(2), obj4);
        final Object obj5 = getProperty(company, "top(-1)");
        assertEquals("Unable to call method ", company.getTop(-1), obj5);
        final Object obj6 = getProperty(company, "bottom(-2)");
        assertEquals("Unable to call method ", company.bottom(-2), obj6);

        // TODO: unsupported ...
        //setProperty(company.getDirector(), "someone(a1)", "A1");
        //assertEquals("Invalid dynamic assignment", "A1", company.getDirector().getSomeone("a1"));
        //setProperty(company.getDirector(), "method1(2)", "ASD");
    }


    public static class Company {
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

    public static class Person {
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

    public static class Address implements Serializable {
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
