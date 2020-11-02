package com.manywords.softworks.tafl.engine.ai;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AiThreadPool {
    private int mThreadCount;
    private List<WorkerThread> mThreads = new ArrayList<WorkerThread>();
    private LinkedList<Runnable> mQueue = new LinkedList<Runnable>();
    private boolean mWaiting = true;

    public AiThreadPool(int numThreads) {
        mThreadCount = numThreads;

        for (int i = 0; i < mThreadCount; i++) {
            mThreads.add(new WorkerThread());
        }
    }

    public void start() {
        mWaiting = true;
        for (WorkerThread t : mThreads) {
            t.start();
        }
    }

    public void execute(Runnable r) {
        mQueue.push(r);
        mWaiting = false;
    }

    public boolean checkFinished() {
        if (mWaiting == true || mQueue.size() > 0) {
            return false;
        } else {
            return true;
        }
    }

    public void requestStop() {
        for (WorkerThread t : mThreads) {
            t.stopSelf();
        }
    }

    private class WorkerThread extends Thread {
        private boolean mStopRequested = false;
        private boolean mIdle = true;

        public void stopSelf() {
            mStopRequested = true;
        }

        public boolean isIdle() {
            return mIdle;
        }

        @Override
        public void run() {
            mIdle = false;
            while (!mStopRequested) {
                Runnable task = mQueue.poll();
                //startTask();

                if (task == null) {
                    mIdle = true;
                    continue;
                }

                mIdle = false;
                task.run();
                //finishTask();
            }
        }
    }
}
