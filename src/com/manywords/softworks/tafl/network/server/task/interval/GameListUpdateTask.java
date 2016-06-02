package com.manywords.softworks.tafl.network.server.task.interval;

import com.manywords.softworks.tafl.network.packet.pregame.ClientListPacket;
import com.manywords.softworks.tafl.network.packet.pregame.GameListPacket;
import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.network.server.ServerClient;

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
        // Don't send lobby updates to a client in the game UI.
        if(mClient.getGame() == null || !mClient.getGame().isGameInProgress()) {
            mClient.writePacket(GameListPacket.parse(mServer.getGames()));
            mClient.writePacket(ClientListPacket.parse(mServer.getLobbyClients()));
        }
    }
}
