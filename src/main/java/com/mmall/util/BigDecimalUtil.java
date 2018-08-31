package com.mmall.util;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

public class BigDecimalUtil{

    public static BigDecimal add(double a,double b){
        BigDecimal d1 = new BigDecimal(Double.toString(a));
        BigDecimal d2 = new BigDecimal(Double.toString(b));
        return d1.add(d2);
    }

    public static BigDecimal sub(double a,double b){
        BigDecimal d1 = new BigDecimal(Double.toString(a));
        BigDecimal d2 = new BigDecimal(Double.toString(b));
        return d1.subtract(d2);
    }
    public static BigDecimal mul(double a,double b){
        BigDecimal d1 = new BigDecimal(Double.toString(a));
        BigDecimal d2 = new BigDecimal(Double.toString(b));
        return d1.multiply(d2);
    }
    public static BigDecimal div(double a,double b){
        BigDecimal d1 = new BigDecimal(Double.toString(a));
        BigDecimal d2 = new BigDecimal(Double.toString(b));
        return d1.divide(d2,2,BigDecimal.ROUND_HALF_UP); // 四舍五入,保留2位小数

    }
}
