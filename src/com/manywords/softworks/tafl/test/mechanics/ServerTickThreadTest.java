package com.manywords.softworks.tafl.test.mechanics;

import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.network.server.task.interval.BucketedIntervalTaskHolder;
import com.manywords.softworks.tafl.network.server.task.interval.IntervalTask;
import com.manywords.softworks.tafl.network.server.task.interval.IntervalTaskHolder;
import com.manywords.softworks.tafl.network.server.thread.PriorityTaskQueue;
import com.manywords.softworks.tafl.network.server.thread.ServerTickThread;
import com.manywords.softworks.tafl.test.TaflTest;

/**
 * Created by jay on 5/25/16.
 */
public class ServerTickThreadTest extends TaflTest {
    private int mIntervalTicks = 0;
    private int mBucketedIntervalTicks = 0;

    @Override
    public void run() {
        PriorityTaskQueue queue = new PriorityTaskQueue(1);
        ServerTickThread thread = new ServerTickThread();

        thread.addTaskHolder(new TestIntervalHolder(queue, 4900, new IntervalTask() {
            @Override
            public void reset() {

            }

            @Override
            public void run() {
                mIntervalTicks++;
                //System.out.println("Other run " + mIntervalTicks);
            }
        }));

        BucketedIntervalTaskHolder bucketedHolder = new BucketedIntervalTaskHolder(queue, 900, 5);
        bucketedHolder.addBucketTask(new BucketTickTask());
        bucketedHolder.addBucketTask(new BucketTickTask());
        bucketedHolder.addBucketTask(new BucketTickTask());
        bucketedHolder.addBucketTask(new BucketTickTask());
        bucketedHolder.addBucketTask(new BucketTickTask());
        thread.addTaskHolder(bucketedHolder);

        queue.start();
        thread.start();

        try {
            Thread.sleep(8000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assert mIntervalTicks == 1;
        assert 5 <= mBucketedIntervalTicks && mBucketedIntervalTicks <= 10;
    }

    private class BucketTickTask extends IntervalTask {

        @Override
        public void reset() {

        }

        @Override
        public void run() {
            mBucketedIntervalTicks++;
            //System.out.println("Running " + mBucketedIntervalTicks);
        }
    }

    private class TestIntervalHolder extends IntervalTaskHolder {

        public TestIntervalHolder(PriorityTaskQueue queue, int interval, IntervalTask task) {
            super(queue, interval, task);
        }
    }
}
