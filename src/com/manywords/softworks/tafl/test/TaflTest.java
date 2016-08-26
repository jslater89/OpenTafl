package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.OpenTafl;
import com.manywords.softworks.tafl.ui.AbstractUiCallback;

public abstract class TaflTest extends AbstractUiCallback {
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
