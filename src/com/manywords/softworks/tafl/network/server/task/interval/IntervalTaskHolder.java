package com.manywords.softworks.tafl.network.server.task.interval;

import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.network.server.thread.PriorityTaskQueue;

/**
 * Created by jay on 5/25/16.
 */
public class IntervalTaskHolder {
    protected final PriorityTaskQueue queue;
    protected final int interval;
    protected final PriorityTaskQueue.Priority priority;
    private final IntervalTask task;

    protected long mLastRun = -1;

    public IntervalTaskHolder(PriorityTaskQueue queue, int interval, IntervalTask task) {
        this(queue, interval, task, PriorityTaskQueue.Priority.STANDARD);
    }

    public IntervalTaskHolder(PriorityTaskQueue queue, int interval, IntervalTask task, PriorityTaskQueue.Priority priority) {
        this.queue = queue;
        this.interval = interval;
        this.task = task;
        this.priority = priority;

        mLastRun = System.currentTimeMillis();
    }

    public void ping(long time) {
        if(time - mLastRun > interval) {
            mLastRun = time;
            task.reset();
            queue.pushTask(task, priority);
        }
    }
}
