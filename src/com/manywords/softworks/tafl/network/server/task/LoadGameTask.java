package com.manywords.softworks.tafl.network.server.task;

import com.manywords.softworks.tafl.network.packet.ingame.HistoryPacket;
import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.network.server.ServerClient;

/**
 * Created by jay on 7/14/16.
 */
public class LoadGameTask implements Runnable {
    private final NetworkServer mServer;
    private final ServerClient mClient;
    private final HistoryPacket mPacket;
    public LoadGameTask(NetworkServer server, ServerClient client, HistoryPacket historyPacket) {
        mServer = server;
        mClient = client;
        mPacket = historyPacket;
    }

    @Override
    public void run() {
        mClient.getGame().loadGame(mPacket.moves);
    }
}
