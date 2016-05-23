package com.manywords.softworks.tafl.network.server.task;

/**
 * Created by jay on 5/23/16.
 */
public class HandleClientCommunicationTask implements Runnable {
    private String mData;

    public HandleClientCommunicationTask(String data) {
        mData = data;
    }

    @Override
    public void run() {
        System.out.println("Server received: " + mData);
    }
}
