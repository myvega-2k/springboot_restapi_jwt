package com.basic.myrestapi;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class LambdaTest {
    @Test
    public void consumer() {

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
