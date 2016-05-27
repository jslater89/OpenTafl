package com.manywords.softworks.tafl.network.server.task;

import com.manywords.softworks.tafl.network.packet.pregame.StartGamePacket;
import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.network.server.ServerClient;
import com.manywords.softworks.tafl.network.server.ServerGame;
import com.manywords.softworks.tafl.network.server.thread.PriorityTaskQueue;

import java.util.List;

/**
 * Created by jay on 5/26/16.
 */
public class StartGameTask implements Runnable {
    private final NetworkServer server;
    private final List<ServerClient> clients;
    private final ServerGame game;
    private final StartGamePacket packet;

    public StartGameTask(NetworkServer server, ServerGame game, List<ServerClient> clients, StartGamePacket packet) {
        this.server = server;
        this.clients = clients;
        this.packet = packet;
        this.game = game;
    }

    @Override
    public void run() {
        for(ServerClient c : clients) {
            server.sendPacketToClient(c, packet, PriorityTaskQueue.Priority.HIGH);
        }

        server.startGame(game);
    }
}
