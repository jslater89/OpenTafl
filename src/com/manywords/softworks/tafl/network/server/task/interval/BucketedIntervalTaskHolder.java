package com.manywords.softworks.tafl.network.server.task.interval;

import com.manywords.softworks.tafl.engine.XorshiftRandom;
import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.network.server.thread.PriorityTaskQueue;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds N tasks in M buckets. Runs N/M tasks per interval.
 * e.g. a 5-second interval and 6 buckets will run each task once per
 * thirty seconds.
 */
public class BucketedIntervalTaskHolder extends IntervalTaskHolder {
    private List<IntervalTask>[] mBuckets;
    private int mLastBucketRun = 0;

    public BucketedIntervalTaskHolder(NetworkServer server, int interval, int bucketCount) {
        this(server, interval, bucketCount, PriorityTaskQueue.Priority.STANDARD);
    }

    public BucketedIntervalTaskHolder(NetworkServer server, int interval, int bucketCount, PriorityTaskQueue.Priority priority) {
        super(server, interval, null, priority);
        if(bucketCount == 0) throw new IllegalArgumentException();
        mBuckets = new List[bucketCount];
        for(int i = 0; i < bucketCount; i++) {
            mBuckets[i] = new ArrayList<>();
        }
    }

    public void addBucketTask(IntervalTask task) {
        int bucket = new XorshiftRandom().nextInt(mBuckets.length);
        mBuckets[bucket].add(task);
    }

    public void removeBucketTask(IntervalTask task) {
        for(List<IntervalTask> bucket : mBuckets) {
            bucket.remove(task);
        }
    }

    @Override
    public void ping() {
        if(mLastRun == -1) mLastRun = System.currentTimeMillis();

        if(System.currentTimeMillis() - mLastRun > interval) {
            pushBucketTasks();
        }
    }

    private void pushBucketTasks() {
        List<IntervalTask> taskList = mBuckets[mLastBucketRun];
        for(IntervalTask task : taskList) {
            task.reset();
            server.getTaskQueue().pushTask(task, priority);
        }

        mLastBucketRun = (mLastBucketRun + 1) % mBuckets.length;
    }
}
