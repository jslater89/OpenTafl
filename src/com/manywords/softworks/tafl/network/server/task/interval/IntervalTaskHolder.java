package com.manywords.softworks.tafl.network.server.task.interval;

import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.network.server.thread.PriorityTaskQueue;

/**
 * Created by jay on 5/25/16.
 */
public abstract class IntervalTaskHolder {
    protected final int interval;
    protected final PriorityTaskQueue.Priority priority;
    protected final NetworkServer server;
    private final IntervalTask task;

    protected long mLastRun = -1;

    public IntervalTaskHolder(NetworkServer server, int interval, IntervalTask task) {
        this(server, interval, task, PriorityTaskQueue.Priority.STANDARD);
    }

    public IntervalTaskHolder(NetworkServer server, int interval, IntervalTask task, PriorityTaskQueue.Priority priority) {
        this.server = server;
        this.interval = interval;
        this.task = task;
        this.priority = priority;
    }

    public void ping() {
        if(mLastRun == -1) mLastRun = System.currentTimeMillis();

        if(System.currentTimeMillis() - mLastRun > interval) {
            task.reset();
            server.getTaskQueue().pushTask(task, priority);
        }
    }
}
