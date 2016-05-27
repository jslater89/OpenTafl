package com.manywords.softworks.tafl.command.player;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.network.packet.ingame.MoveResultPacket;
import com.manywords.softworks.tafl.network.packet.ingame.MovePacket;
import com.manywords.softworks.tafl.network.packet.ingame.RequestMovePacket;
import com.manywords.softworks.tafl.network.server.ServerClient;
import com.manywords.softworks.tafl.ui.UiCallback;

/**
 * Created by jay on 5/26/16.
 */
public class NetworkServerPlayer extends Player {
    private ServerClient mClient;
    private PlayerCallback mPlayerCallback;

    public NetworkServerPlayer() {

    }

    public void setClient(ServerClient client) {
        this.mClient = client;
    }

    @Override
    public void getNextMove(UiCallback ui, Game game, int thinkTime) {
        mClient.writePacket(new RequestMovePacket(isAttackingSide()));
    }

    @Override
    public void moveResult(int moveResult) {
        mClient.writePacket(new MoveResultPacket(moveResult));
    }

    @Override
    public void opponentMove(MoveRecord move) {
        mClient.writePacket(new MovePacket(move));
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
        mPlayerCallback.onMoveDecided(this, record);
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
