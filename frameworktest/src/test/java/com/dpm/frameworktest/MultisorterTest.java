package com.dpm.frameworktest;

import com.dpm.framework.Multisorter;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MultisorterTest {

    static class Person {
        String name;
        int age;

        Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
    }

    @Test
    public void sort_WithMultipleComparators() {
        List<Person> list = new ArrayList<>(Arrays.asList(
                new Person("Alice", 30),
                new Person("Bob", 25),
                new Person("Alice", 20)
        ));

        List<Comparator<Person>> comparators = new ArrayList<>();
        comparators.add(Comparator.comparingInt(p -> p.age));
        comparators.add(Comparator.comparing(p -> p.name));

        Multisorter.sort(list, comparators);

        // Primary sort key is the last one in the list (Multisorter logic)
        Assert.assertEquals("Alice", list.get(0).name);
        Assert.assertEquals(20, list.get(0).age);
        Assert.assertEquals("Alice", list.get(1).name);
        Assert.assertEquals(30, list.get(1).age);
        Assert.assertEquals("Bob", list.get(2).name);
        Assert.assertEquals(25, list.get(2).age);
    }
}
