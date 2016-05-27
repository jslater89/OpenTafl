package com.manywords.softworks.tafl.network.server.task;

import com.manywords.softworks.tafl.network.packet.pregame.JoinGamePacket;
import com.manywords.softworks.tafl.network.packet.utility.ErrorPacket;
import com.manywords.softworks.tafl.network.packet.utility.SuccessPacket;
import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.network.server.ServerClient;
import com.manywords.softworks.tafl.network.server.ServerGame;
import com.manywords.softworks.tafl.network.server.thread.PriorityTaskQueue;

/**
 * Created by jay on 5/26/16.
 */
public class JoinGameTask implements Runnable {
    private final NetworkServer mServer;
    private final ServerClient mClient;
    private final JoinGamePacket mPacket;

    public JoinGameTask(NetworkServer mServer, ServerClient mClient, JoinGamePacket parse) {
        this.mServer = mServer;
        this.mClient = mClient;
        mPacket = parse;
    }

    @Override
    public void run() {
        ServerGame g = mServer.getGame(mPacket.uuid);

        if(mClient.getGame() != null) {
            mServer.sendPacketToClient(mClient, new ErrorPacket(ErrorPacket.ALREADY_HOSTING), PriorityTaskQueue.Priority.LOW);
        }
        else if(g != null) {
            boolean result = g.tryPassword(mPacket.hashedPassword);
            if(!result) {
                mServer.sendPacketToClient(mClient, new ErrorPacket(ErrorPacket.INVALID_GAME_PASSWORD), PriorityTaskQueue.Priority.LOW);
            }

            result = g.tryJoinGame(mClient, mPacket.hashedPassword);
            if(!result) {
                mServer.sendPacketToClient(mClient, new ErrorPacket(ErrorPacket.GAME_FULL), PriorityTaskQueue.Priority.LOW);
            }
            else {
                mServer.sendPacketToClient(mClient, new SuccessPacket(), PriorityTaskQueue.Priority.LOW);
            }
        }
        else {
            mServer.sendPacketToClient(mClient, new ErrorPacket(ErrorPacket.GAME_CANCELED), PriorityTaskQueue.Priority.LOW);
        }
    }
}
