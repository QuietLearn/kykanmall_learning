package com.mmall;

import org.junit.Test;

import java.math.BigDecimal;

public class test {

    @Test
    public void fun1(){
        BigDecimal a1 = new BigDecimal("0.01");
        BigDecimal a2 = new BigDecimal("0.05");
        System.out.println(a1.add(a2));
    }
}
