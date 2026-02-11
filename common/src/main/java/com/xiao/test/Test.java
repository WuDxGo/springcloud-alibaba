package com.xiao.test;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        //testThread();
        testLambda();
    }

    /**
     * 线程测试Join
     */
    public static void testThread() {
        Thread thread = new Thread(() -> System.out.println("Hello WorldA"));
        Thread thread1 = new Thread(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("Hello WorldB");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        Thread thread3 = new Thread(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("Hello WorldC");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        thread1.start();
        try {
            thread1.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        thread3.start();
        try {
            thread3.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        thread.start();
    }

    /**
     * 测试Lambda
     */
    public static void testLambda() {
        /*ILove love = (name, who) -> System.out.println(name + who);
        love.love("I Love", " You");*/

        //输出一个参数的时候，可以简写
        ILove2 love2 = System.out::println;
        love2.love("I Love You");

        List<String> list = new ArrayList<>();
        list.add("张三");
        list.add("李四");
        list.forEach(name -> {
                    list.set(0, "小飞");
                    list.set(1, "小黑");
                }
        );
        System.out.println(list);
        System.out.println(list.spliterator());
    }
}

interface ILove {
    void love(String name, String who);
}

interface ILove2 {
    void love(String name);
}

@Data
class Student {
    private String name;
    private int age;
}