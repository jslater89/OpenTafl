package com.manywords.softworks.tafl.network.server.task;

import com.manywords.softworks.tafl.network.packet.pregame.CancelGamePacket;
import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.network.server.ServerClient;
import com.manywords.softworks.tafl.network.server.ServerGame;

/**
 * Created by jay on 5/26/16.
 */
public class CancelGameTask implements Runnable {
    private final NetworkServer server;
    private final ServerClient client;
    private final CancelGamePacket packet;

    public CancelGameTask(NetworkServer server, ServerClient client, CancelGamePacket packet) {
        this.packet = packet;
        this.client = client;
        this.server = server;
    }

    @Override
    public void run() {
        ServerGame g = server.getGame(packet.uuid);

        g.cancel(client);
    }
}
