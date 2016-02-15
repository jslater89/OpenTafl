package com.manywords.softworks.tafl.ui.player;

/**
 * Created by jay on 2/15/16.
 */
public class PlayerWorkerThread extends Thread {
    public abstract static class PlayerWorkerRunnable implements Runnable {
        public abstract void cancel();
    }

    private PlayerWorkerRunnable mRunnable;

    public PlayerWorkerThread(PlayerWorkerRunnable r) {
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
