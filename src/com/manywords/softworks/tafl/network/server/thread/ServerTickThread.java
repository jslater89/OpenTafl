package com.manywords.softworks.tafl.network.server.thread;

import com.manywords.softworks.tafl.network.server.task.interval.IntervalTaskHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 5/25/16.
 */
public class ServerTickThread extends Thread {
    private List<IntervalTaskHolder> mIntervalTaskHolders = new ArrayList<>();
    private static final int TICK_TIME = 1000; //ms
    private boolean mRunning = true;

    public void addTaskHolder(IntervalTaskHolder taskHolder) {
        mIntervalTaskHolders.add(taskHolder);
    }

    public void removeTaskHolder(IntervalTaskHolder taskHolder) {
        mIntervalTaskHolders.remove(taskHolder);
    }

    @Override
    public void run() {
        while(mRunning) {
            try {
                Thread.sleep(TICK_TIME);
            } catch (InterruptedException e) {
                // Don't care
            }

            for (IntervalTaskHolder task : mIntervalTaskHolders) {
                task.ping();
            }
        }
    }
}
