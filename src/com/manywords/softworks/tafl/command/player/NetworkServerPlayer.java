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
    /*
     * Path of a move on the server:
     *
     * 1. Client A sends a move packet.
     * 2. ServerClient for Client A enqueues a HandleCommunicationTask.
     * 3. HandleCommunicationTask enqueues a MoveTask.
     * 4. MoveTask calls NetworkServerPlayer.onMoveDecided.
     * 5. onMoveDecided calls to the ServerGame's command engine.
     * 6. The command engine makes the move.
     * 7. The command engine sends the result via callback to the opposing Player.
     * 8. The opposing NetworkServerPlayer's opponentMove task queues a send packet task
     *    to send the move to the opposing player.
     * 9. The MoveTask queues send packet tasks to send the move to spectators.
     */
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
        // ServerClient calls here
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
