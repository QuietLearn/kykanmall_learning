package com.mmall;

import com.google.common.collect.Lists;
import com.mmall.pojo.Category;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class test {

    @Test
    public void fun1(){
        BigDecimal a1 = new BigDecimal("0.01");
        BigDecimal a2 = new BigDecimal("0.05");
        System.out.println(a1.add(a2));
        Map<String,String> map = new HashMap();
        for (Map.Entry<String,String> entry:map.entrySet()) {
            
        }
    }


    @Test
    public void fun2(){
        List<String> stringList = Lists.newArrayList();
        for (String s:stringList) {
            System.out.println(1);
        }
        System.out.println(1211122133);
    }

    @Test
    public void fun3(){
        Category category =null;
        try {
            category = Category.class.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        category.setId(3);
        int i = category.hashCode();

        System.out.println(1211122133);
    }


    @Test
    public void fun4(){

        HashMap<Person,String> map = new HashMap<Person, String>();
        Person person = new Person(1234,"乔峰");
        //put到hashmap中去
        map.put(person,"天龙八部");
        //get取出，从逻辑上讲应该能输出“天龙八部”
        System.out.println("结果:"+map.get(new Person(1234,"萧峰")));
    }

    @Test
    public void fun6(){

    }


    private static class Person{
        int idCard;
        String name;

        public Person(int idCard, String name) {
            this.idCard = idCard;
            this.name = name;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()){
                return false;
            }
            Person person = (Person) o;
            //两个对象是否等值，通过idCard来确定
            return this.idCard == person.idCard;
        }

    }
}
