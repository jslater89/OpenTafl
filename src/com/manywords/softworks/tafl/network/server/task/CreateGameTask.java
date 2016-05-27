package com.manywords.softworks.tafl.network.server.task;

import com.manywords.softworks.tafl.network.packet.pregame.CreateGamePacket;
import com.manywords.softworks.tafl.network.packet.utility.ErrorPacket;
import com.manywords.softworks.tafl.network.packet.utility.SuccessPacket;
import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.network.server.ServerClient;
import com.manywords.softworks.tafl.network.server.thread.PriorityTaskQueue;
import com.manywords.softworks.tafl.notation.RulesSerializer;

/**
 * Created by jay on 5/26/16.
 */
public class CreateGameTask implements Runnable {
    private NetworkServer mServer;
    private ServerClient mClient;
    private CreateGamePacket mPacket;

    public CreateGameTask(NetworkServer server, ServerClient client, CreateGamePacket packet) {
        mServer = server;
        mClient = client;
        mPacket = packet;
    }

    @Override
    public void run() {
        boolean result = mServer.createGame(mClient, mPacket.uuid, mPacket.passwordHash, RulesSerializer.loadRulesRecord(mPacket.otnRulesString), mPacket.attackingSide);

        if(result) {
            mServer.sendPacketToClient(mClient, new SuccessPacket(), PriorityTaskQueue.Priority.STANDARD);
        }
        else {
            mServer.sendPacketToClient(mClient, new ErrorPacket(ErrorPacket.ALREADY_HOSTING), PriorityTaskQueue.Priority.LOW);
        }
    }
}
