package com.manywords.softworks.tafl.test;

public abstract class TaflTest {
    public void println(Object o) {
        System.out.println(o);
    }

    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public abstract void run();
}
