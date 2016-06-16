package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.OpenTafl;

public abstract class TaflTest {
    public void println(Object o) {
        System.out.println(o);
    }

    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            OpenTafl.logStackTrace(OpenTafl.LogLevel.CHATTY, e);
        }
    }

    public abstract void run();
}
