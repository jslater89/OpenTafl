package com.manywords.softworks.tafl.network.server.task;

import com.manywords.softworks.tafl.network.packet.ErrorPacket;
import com.manywords.softworks.tafl.network.packet.LoginPacket;
import com.manywords.softworks.tafl.network.packet.SuccessPacket;
import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.network.server.ServerClient;
import com.manywords.softworks.tafl.network.server.thread.PriorityTaskQueue;

/**
 * Created by jay on 5/23/16.
 */
public class LoginTask implements Runnable {
    private final LoginPacket packet;
    private final NetworkServer server;
    private final ServerClient client;

    public LoginTask(NetworkServer server, ServerClient client, LoginPacket packet) {
        this.packet = packet;
        this.server = server;
        this.client = client;
    }

    @Override
    public void run() {
        // if login good
        if (server.getPlayerDatabase().allowLoginFor(packet.username, packet.password)) {
            client.onRegistered(packet.username);
            server.sendPacketToClient(client, new SuccessPacket(), PriorityTaskQueue.Priority.LOW);
        }
        else {
            server.sendPacketToClient(client, new ErrorPacket(ErrorPacket.LOGIN_FAILED), PriorityTaskQueue.Priority.LOW);
        }
    }
}
