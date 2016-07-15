package com.manywords.softworks.tafl.network.server.task;

import com.manywords.softworks.tafl.network.packet.ingame.ClockUpdatePacket;
import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.network.server.ServerClient;

/**
 * Created by jay on 7/15/16.
 */
public class InitialTimeSettingTask implements Runnable {
    private NetworkServer mServer;
    private ServerClient mClient;
    private ClockUpdatePacket mPacket;

    public InitialTimeSettingTask(NetworkServer server, ServerClient client, ClockUpdatePacket packet) {
        mServer = server;
        mClient = client;
        mPacket = packet;
    }

    @Override
    public void run() {
        mClient.getGame().setInitialTime(mPacket.attackerClock, mPacket.defenderClock);
    }
}
