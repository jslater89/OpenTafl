package com.manywords.softworks.tafl.command.player;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.network.packet.ingame.MoveResultPacket;
import com.manywords.softworks.tafl.network.packet.ingame.MovePacket;
import com.manywords.softworks.tafl.network.packet.ingame.AwaitMovePacket;
import com.manywords.softworks.tafl.network.server.NetworkServer;
import com.manywords.softworks.tafl.network.server.ServerClient;
import com.manywords.softworks.tafl.network.server.thread.PriorityTaskQueue;
import com.manywords.softworks.tafl.ui.UiCallback;

/**
 * Created by jay on 5/26/16.
 */
public class NetworkServerPlayer extends Player {
    private ServerClient mClient;
    private PlayerCallback mPlayerCallback;
    private NetworkServer mServer;

    public NetworkServerPlayer(NetworkServer server) {
        this.mServer = server;
    }

    public void setClient(ServerClient client) {
        this.mClient = client;
    }

    @Override
    public void getNextMove(UiCallback ui, Game game, int thinkTime) {
        mServer.sendPacketToClient(mClient, new AwaitMovePacket(isAttackingSide()), PriorityTaskQueue.Priority.HIGH);
    }

    @Override
    public void moveResult(int moveResult) {
        mServer.sendPacketToClient(mClient, new MoveResultPacket(moveResult), PriorityTaskQueue.Priority.HIGH);
    }

    @Override
    public void opponentMove(MoveRecord move) {
        mServer.sendPacketToClient(mClient, new MovePacket(move), PriorityTaskQueue.Priority.HIGH);
    }

    @Override
    public void stop() {
        // No worker threads, woo!
    }

    @Override
    public void timeUpdate() {
        // Handled elsewhere
    }

    @Override
    public void onMoveDecided(MoveRecord record) {
        // Client calls into here
        synchronized(mClient.getGame()) {
            mPlayerCallback.onMoveDecided(this, record);
        }
    }

    @Override
    public void setCallback(PlayerCallback callback) {
        mPlayerCallback = callback;
    }

    @Override
    public Type getType() {
        return Type.NETWORK_SERVER;
    }
}
