package org.echosoft.common.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Anton Sharapov
 */
public class BeanUtilPerfMeter {

    private static final int SERIES_CNT = 5;
    private static final int SERIES_ITER_CNT = 100000;

    private static final Company company;
    static {
        company = new Company("Company 1", "1001", new Person("Ivanov", 1, new Address("russia", "moscow", 101)));
        company.employee.add(new Person("Petrov", 2, null));
    }

    public static void main(final String[] args) throws Exception {
        System.out.println("warm up code ...");
        for (int i = 1000; i >= 0; i--) {
            test1(100);
            test2(100);
        }
        System.out.println("start tests ...");
        final long[] times1 = new long[SERIES_CNT];
        final long[] times2 = new long[SERIES_CNT];
        long sum1 = 0, sum2 = 0;
        for (int i = SERIES_CNT - 1; i >= 0; i--) {
            times1[i] = test1(SERIES_ITER_CNT);
            times2[i] = test2(SERIES_ITER_CNT);
            System.out.println(i + " iteration:   " + times1[i] + " -> " + times2[i]);
            sum1 += times1[i];
            sum2 += times2[i];
        }
        System.out.println("TOTAL:");
        System.out.println("1st implementation average time = " + (sum1 / SERIES_CNT));
        System.out.println("2nd implementation average time = " + (sum2 / SERIES_CNT));
    }

//    private final Runnable T1 = new Runnable() {
//        @Override
//        public void run() {
//            test1(SERIES_CNT);
//        }
//    };

    private static long test1(final int counter) throws Exception {
        final long started = System.currentTimeMillis();
        for (int i = counter; i >= 0; i--) {
            BeanUtil.getProperty(company, "cid");
            BeanUtil.getProperty(company, "name");
            BeanUtil.getProperty(company, "director");
            BeanUtil.getProperty(company, "director.name");
            BeanUtil.getProperty(company, "director.address.country");
            BeanUtil.getProperty(company, "director.topRated");
            BeanUtil.getProperty(company, "director.awards[2]");
            BeanUtil.getProperty(company, "director.awards[1].bytes");
            BeanUtil.getProperty(company, "director.skills[0]");
            BeanUtil.getProperty(company, "director.env.k1");
            BeanUtil.getProperty(company, "director.getEnv.k2");
            BeanUtil.getProperty(company, "hasChildren");
            BeanUtil.getProperty(company, "director.address.hashCode");
            BeanUtil.getProperty(company, "director.getSomeone(keystr)");
            BeanUtil.getProperty(company, "director.someone(keystr)");
            BeanUtil.getProperty(company, "director.getMethod1(1)");
            BeanUtil.getProperty(company, "director.method1(2)");
            BeanUtil.getProperty(company, "director.getMethod2(1)");
            BeanUtil.getProperty(company, "director.method2(2)");
            BeanUtil.getProperty(company, "top(-1)");
            BeanUtil.getProperty(company, "bottom(-2)");
        }
        return System.currentTimeMillis() - started;
    }

    private static long test2(final int counter) throws Exception {
        final long started = System.currentTimeMillis();
        for (int i = counter; i >= 0; i--) {
            BeanUtil.getProperty(company, "cid");
            BeanUtil.getProperty(company, "name");
            BeanUtil.getProperty(company, "director");
            BeanUtil.getProperty(company, "director.name");
            BeanUtil.getProperty(company, "director.address.country");
            BeanUtil.getProperty(company, "director.topRated");
            BeanUtil.getProperty(company, "director.awards[2]");
            BeanUtil.getProperty(company, "director.awards[1].bytes");
            BeanUtil.getProperty(company, "director.skills[0]");
            BeanUtil.getProperty(company, "director.env.k1");
            BeanUtil.getProperty(company, "director.getEnv.k2");
            BeanUtil.getProperty(company, "hasChildren");
            BeanUtil.getProperty(company, "director.address.hashCode");
            BeanUtil.getProperty(company, "director.getSomeone(keystr)");
            BeanUtil.getProperty(company, "director.someone(keystr)");
            BeanUtil.getProperty(company, "director.getMethod1(1)");
            BeanUtil.getProperty(company, "director.method1(2)");
            BeanUtil.getProperty(company, "director.getMethod2(1)");
            BeanUtil.getProperty(company, "director.method2(2)");
            BeanUtil.getProperty(company, "top(-1)");
            BeanUtil.getProperty(company, "bottom(-2)");
        }
        return System.currentTimeMillis() - started;
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
            employee.add(director);
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
        private Map<Object, Object> env;
        private List<String> skills;
        private String awards[];

        public Person(String name, int rate, Address address) {
            this.name = name;
            this.rate = rate;
            this.address = address;
            this.env = new HashMap<Object, Object>();
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
            return rate > 0;
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
