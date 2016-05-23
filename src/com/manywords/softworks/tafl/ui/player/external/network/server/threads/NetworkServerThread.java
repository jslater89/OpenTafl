package com.manywords.softworks.tafl.ui.player.external.network.server.threads;

/**
 * Created by jay on 5/22/16.
 */
public class NetworkServerThread extends Thread {
    private boolean mWaiting;
    private boolean mRunning = true;
    private NetworkPriorityTaskQueue mQueue;

    @Override
    public void run() {
        Runnable task = null;

        // Get a task from the queue and run it. If
        // there is no task to run, wait. The system
        // will notify us when there's something for
        // us to do.
        while(mRunning) {
            while ((task = mQueue.getTask()) != null) {
                task.run();
            }

            waitForTask();
        }
    }

    private void waitForTask() {
        try {
            this.wait();
            mWaiting = true;
        } catch (InterruptedException e) {

        }
    }

    public void notifyThisThread() {
        this.notify();

        mWaiting = false;
    }

    public boolean isWaiting() {
        return mWaiting;
    }
}
