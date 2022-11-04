package com.basic.myrestapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class LambdaTest {
    @Data @AllArgsConstructor
    static class Customer {
        String name;
        int age;
    }

    @Test
    public void stream() {
        List<Customer> customers = List.of(new Customer("둘리", 10),
                new Customer("짱구", 20),
                new Customer("길동", 30));
        //1. Customer Name List<String>
        List<String> stringList = customers.stream() //Stream<Customer>
                //.map(cust -> cust.getName()) //Stream<Customer>
                .map(Customer::getName) //Stream<String>
                .collect(Collectors.toList());//List<String>

        stringList.forEach(System.out::println);
        //2. Customer Age Sum
        int sum = customers.stream() //Stream<Customer>
                //.mapToInt(cust -> cust.getAge())  //IntStream
                .filter(cust -> cust.getAge() > 10)
                .mapToInt(Customer::getAge)
                .sum();
        System.out.println("sum = " + sum);
    }

    @Test @Disabled
    public void consumer() {
        //Immutable List
        List<String> list = List.of("aa", "bb", "ccc");
        //list.add("dd");
        //1. Anonymous Inner Class
        list.forEach(new Consumer<String>() {
            @Override
            public void accept(String s) {
                System.out.println("s = " + s);
            }
        });
        //2. Lambda Expression
        list.forEach(val -> System.out.println(val));
        //3. Method Reference
        list.forEach(System.out::println);
    }


    @Test @Disabled
    public void runnable() {
        //1. Anonymous Inner Class
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Anonymous Inner Class");
            }
        });
        t1.start();
        //2. Lambda Expression
        Thread t2 = new Thread(() -> System.out.println("Lambda Expression"));
        t2.start();
    }
}
