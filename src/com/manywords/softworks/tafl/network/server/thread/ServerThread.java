package com.manywords.softworks.tafl.network.server.thread;

/**
 * Created by jay on 5/22/16.
 */
public class ServerThread extends Thread {
    private boolean mWaiting;
    private boolean mRunning = true;
    private PriorityTaskQueue mQueue;

    public ServerThread(String name, PriorityTaskQueue queue) {
        super(name);
        mQueue = queue;
    }

    @Override
    public void run() {
        Runnable task = null;

        // Get a task from the queue and run it. If
        // there is no task to run, wait. The system
        // will notify us when there's something for
        // us to do.
        while(mRunning) {
            while ((task = mQueue.getTask()) != null) {
                try {
                    task.run();
                }
                catch(Exception e) {
                    System.out.println("Encountered exception reading from client: ");
                    e.printStackTrace(System.out);
                }
            }

            waitForTask();
        }
    }

    private synchronized void waitForTask() {
        mWaiting = true;
        while(mWaiting) {
            try {
                this.wait();
            } catch (InterruptedException e) {
            }
        }
    }

    public synchronized void notifyThisThread() {
        mWaiting = false;
        this.notify();
    }

    public boolean isWaiting() {
        return mWaiting;
    }
}
