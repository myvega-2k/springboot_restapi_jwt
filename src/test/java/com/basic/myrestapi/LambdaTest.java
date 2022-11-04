package com.basic.myrestapi;

import org.junit.jupiter.api.Test;

public class LambdaTest {
    @Test
    public void runnable() {
        //1. Anonymous Inner Class
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Anonymous Inner Class");
            }
        });
        t1.start();

    }
}
