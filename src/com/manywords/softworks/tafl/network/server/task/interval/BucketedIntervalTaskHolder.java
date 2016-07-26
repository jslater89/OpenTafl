package com.manywords.softworks.tafl.network.server.task.interval;

import com.manywords.softworks.tafl.engine.XorshiftRandom;
import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.network.server.thread.PriorityTaskQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Holds N tasks in M buckets. Runs N/M tasks per interval.
 * e.g. a 5-second interval and 6 buckets will run each task once per
 * thirty seconds.
 */
public class BucketedIntervalTaskHolder extends IntervalTaskHolder {
    private List<IntervalTask>[] mBuckets;
    private int mLastBucketRun = 0;
    private final Random r;

    public BucketedIntervalTaskHolder(PriorityTaskQueue queue, int interval, int bucketCount) {
        this(queue, interval, bucketCount, PriorityTaskQueue.Priority.STANDARD);
    }

    public BucketedIntervalTaskHolder(PriorityTaskQueue queue, int interval, int bucketCount, PriorityTaskQueue.Priority priority) {
        super(queue, interval, null, priority);
        if(bucketCount == 0) throw new IllegalArgumentException();
        mBuckets = new List[bucketCount];
        for(int i = 0; i < bucketCount; i++) {
            mBuckets[i] = new ArrayList<>();
        }

        mLastRun = System.currentTimeMillis();
        r = new XorshiftRandom();
    }

    public synchronized void addBucketTask(IntervalTask task) {
        if(!containsBucketTask(task)) {
            int bucket = r.nextInt(mBuckets.length);
            mBuckets[bucket].add(task);
        }
    }

    public synchronized boolean containsBucketTask(IntervalTask task) {
        for(List<IntervalTask> bucket : mBuckets) {
            if(bucket.contains(task)) return true;
        }

        return false;
    }

    public synchronized void removeBucketTask(IntervalTask task) {
        for(List<IntervalTask> bucket : mBuckets) {
            bucket.remove(task);
        }
    }

    @Override
    public void ping(long time) {
        if(time - mLastRun > interval) {
            mLastRun = time;
            pushBucketTasks();
        }
    }

    private synchronized void pushBucketTasks() {
        List<IntervalTask> taskList = mBuckets[mLastBucketRun];
        for(IntervalTask task : taskList) {
            task.reset();
            queue.pushTask(task, priority);
        }

        mLastBucketRun = (mLastBucketRun + 1) % mBuckets.length;
    }
}
