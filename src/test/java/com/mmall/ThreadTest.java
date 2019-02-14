package com.mmall;

import java.lang.reflect.Field;

class InitClass{
    static {
        System.out.println("初始化InitClass");
    }
    public static String a = null;
    public static void method(){}
}

class SubInitClass extends InitClass{}

public class ThreadTest {
    public static int i = 0;
    public static void print() {
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            System.out.print("step " + i + " is running.");
            sum += i;
        }
        if (sum != 45) {
            System.out.println("Thread error!");
            System.exit(0);
        }
        System.out.println("sum is " + sum);

    }

    public void fun(){
        System.out.println(1);
    }

    public static void main(String[] args) {
        //  主动引用引起类的初始化一: new对象、读取或设置类的静态变量、调用类的静态方法。
        //	new InitClass();
        //	InitClass.a = "";
        //	String a = InitClass.a;
        //	InitClass.method();
//  主动引用引起类的初始化二：通过反射实例化对象、读取或设置类的静态变量、调用类的静态方法。
        	Class cls = InitClass.class;
        try {
            cls.newInstance();
            Field f = cls.getDeclaredField("a");
            f.get(null);
            f.set(null, "s");
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }


        //	Method md = cls.getDeclaredMethod("method");
        //	md.invoke(null, null);

        //  主动引用引起类的初始化三：实例化子类，引起父类初始化。
        //	new SubInitClass();

    }
}
