package com.manywords.softworks.tafl.network.server.task;

import com.manywords.softworks.tafl.network.server.ServerClient;
import com.manywords.softworks.tafl.network.packet.NetworkPacket;

/**
 * Created by jay on 5/23/16.
 */
public class SendPacketTask implements Runnable{
    private final NetworkPacket mTask;
    private final ServerClient mDestination;

    public SendPacketTask(NetworkPacket task, ServerClient destination) {
        mTask = task;
        mDestination = destination;
    }

    @Override
    public void run() {
        mDestination.writePacket(mTask);
    }
}
