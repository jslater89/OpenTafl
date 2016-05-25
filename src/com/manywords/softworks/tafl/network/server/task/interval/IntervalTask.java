package com.manywords.softworks.tafl.network.server.task.interval;

/**
 * Created by jay on 5/25/16.
 */
public abstract class IntervalTask implements Runnable {
    // Prepare for a new run.
    public abstract void reset();
}
