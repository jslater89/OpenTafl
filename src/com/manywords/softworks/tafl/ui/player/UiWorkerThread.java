package com.manywords.softworks.tafl.ui.player;

/**
 * Created by jay on 2/15/16.
 */
public class UiWorkerThread extends Thread {
    public abstract static class UiWorkerRunnable implements Runnable {
        public abstract void cancel();
    }

    private UiWorkerRunnable mRunnable;

    public UiWorkerThread(UiWorkerRunnable r) {
        super(r);
        mRunnable = r;
    }

    public Runnable getPlayerWorkerRunnable() {
        return mRunnable;
    }

    public void cancel() {
        mRunnable.cancel();
    }
}
