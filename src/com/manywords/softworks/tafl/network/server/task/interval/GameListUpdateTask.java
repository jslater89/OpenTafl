package com.manywords.softworks.tafl.network.server.task.interval;

import com.manywords.softworks.tafl.network.NetworkDummyDataGenerator;
import com.manywords.softworks.tafl.network.packet.GameListPacket;
import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.network.server.ServerClient;

import java.util.Random;

/**
 * Created by jay on 5/25/16.
 */
public class GameListUpdateTask extends IntervalTask {
    private final NetworkServer mServer;
    private final ServerClient mClient;

    public GameListUpdateTask(NetworkServer server, ServerClient client) {
        mServer = server;
        mClient = client;
    }

    @Override
    public void reset() {

    }

    @Override
    public void run() {
        mClient.writePacket(GameListPacket.parse(mServer.getGames()));
        //mClient.writePacket(GameListPacket.parse(NetworkDummyDataGenerator.getDummyGames(mServer, new Random().nextInt(25) + 1)));
    }
}
