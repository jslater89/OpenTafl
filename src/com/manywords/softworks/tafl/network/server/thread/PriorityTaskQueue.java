package com.manywords.softworks.tafl.network.server.thread;

import com.manywords.softworks.tafl.network.server.NetworkServer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 5/22/16.
 */
public class PriorityTaskQueue {
    public enum Priority {
        HIGH,
        STANDARD,
        LOW
    }

    private List<ServerThread> mThreadPool;

    private final List<Runnable> mHighPriorityQueue;
    private final List<Runnable> mStandardPriorityQueue;
    private final List<Runnable> mLowPriorityTaskQueue;

    public PriorityTaskQueue(int threadCount) {
        mThreadPool = new ArrayList<>(threadCount);
        mHighPriorityQueue = new ArrayList<>(16);
        mStandardPriorityQueue = new ArrayList<>(32);
        mLowPriorityTaskQueue = new ArrayList<>(16);

        for(int i = 0; i < threadCount; i++) {
            mThreadPool.add(new ServerThread("ServerThread" + i, this));
        }
    }

    public void start() {
        for(ServerThread thread : mThreadPool) {
            thread.start();
        }
    }


    private void notifyThreadIfNecessary() {
        for(ServerThread thread : mThreadPool) {
            if(thread.isWaiting()) {
                thread.notifyThisThread();
                return;
            }
        }
    }

    public void pushTask(Runnable task, Priority priority) {
        switch(priority) {

            case HIGH:
                pushHighPriorityTask(task);
                break;
            case STANDARD:
                pushStandardPriorityTask(task);
                break;
            case LOW:
                pushLowPriorityTask(task);
                break;
        }
    }

    private void pushHighPriorityTask(Runnable task) {
        synchronized (mHighPriorityQueue) {
            mHighPriorityQueue.add(task);
        }

        notifyThreadIfNecessary();
    }

    private void pushStandardPriorityTask(Runnable task) {
        synchronized (mStandardPriorityQueue) {
            mStandardPriorityQueue.add(task);
        }

        notifyThreadIfNecessary();
    }

    private void pushLowPriorityTask(Runnable task) {
        synchronized (mLowPriorityTaskQueue) {
            mLowPriorityTaskQueue.add(task);
        }

        notifyThreadIfNecessary();
    }

    public Runnable getTask() {
        // Take a task from the highest priority queue with tasks, unless a lower priority's queue is 4^depth times larger
        // than the queue in question.

        try {
            if (mHighPriorityQueue.size() > 0
                    && !((mStandardPriorityQueue.size() > mHighPriorityQueue.size() * 4) || (mLowPriorityTaskQueue.size() > mHighPriorityQueue.size() * 16))) {
                synchronized (mHighPriorityQueue) {
                    return mHighPriorityQueue.remove(0);
                }
            }
            else if (mStandardPriorityQueue.size() > 0 && !(mLowPriorityTaskQueue.size() > mStandardPriorityQueue.size() * 4)) {
                synchronized (mStandardPriorityQueue) {
                    return mStandardPriorityQueue.remove(0);
                }
            }
            else if (mLowPriorityTaskQueue.size() > 0) {
                synchronized (mLowPriorityTaskQueue) {
                    return mLowPriorityTaskQueue.remove(0);
                }
            }
        }
        catch(IndexOutOfBoundsException e) {
            // Race condition possible here, but I don't want to synchronize the whole block for speed reasons.
        }

        return null;
    }
}
