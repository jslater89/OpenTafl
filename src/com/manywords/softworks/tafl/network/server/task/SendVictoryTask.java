package com.manywords.softworks.tafl.network.server.task;


import com.manywords.softworks.tafl.network.packet.ingame.VictoryPacket;
import com.manywords.softworks.tafl.network.server.GameRole;
import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.network.server.ServerClient;
import com.manywords.softworks.tafl.network.server.ServerGame;
import com.manywords.softworks.tafl.network.server.thread.PriorityTaskQueue;

/**
 * Created by jay on 5/29/16.
 */
public class SendVictoryTask implements Runnable {
    private final NetworkServer server;
    private final ServerClient client;
    private final VictoryPacket packet;


    public SendVictoryTask(NetworkServer server, ServerClient client, VictoryPacket packet) {
        this.server = server;
        this.client = client;
        this.packet = packet;
    }

    public static void sendOnClientLeaving(NetworkServer server, ServerClient clientLeavingGame) {
        if(clientLeavingGame.getGame() == null) throw new RuntimeException();

        ServerGame g = clientLeavingGame.getGame();
        VictoryPacket p = null;
        if(clientLeavingGame.getGameRole() == GameRole.ATTACKER) {
            server.sendPacketToClients(g.getAllClients(), new VictoryPacket(VictoryPacket.Victory.DEFENDER), PriorityTaskQueue.Priority.STANDARD);
        }
        else if(clientLeavingGame.getGameRole() == GameRole.DEFENDER) {
            server.sendPacketToClients(g.getAllClients(), new VictoryPacket(VictoryPacket.Victory.ATTACKER), PriorityTaskQueue.Priority.STANDARD);
        }
    }

    @Override
    public void run() {
        server.sendPacketToClient(client, packet, PriorityTaskQueue.Priority.STANDARD);
    }
}
