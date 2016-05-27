package com.manywords.softworks.tafl.command.player;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.network.client.ClientServerConnection;
import com.manywords.softworks.tafl.network.server.GameRole;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.network.server.NetworkServer;

/**
 * Created by jay on 5/22/16.
 */
public class NetworkClientPlayer extends Player {
    private ClientServerConnection mConnection;
    private PlayerCallback mCallback;

    private GameRole mGameRole;

    public NetworkClientPlayer(ClientServerConnection c) {
        mConnection = c;
        mGameRole = c.getGameRole();
    }

    public GameRole getGameRole() {
        return mGameRole;
    }

    @Override
    public void getNextMove(UiCallback ui, Game game, int thinkTime) {
        // No-op
    }

    @Override
    public void moveResult(int moveResult) {
        // No-op
    }

    @Override
    public void opponentMove(MoveRecord move) {
        mConnection.sendMoveDecidedMessage(move);
    }

    @Override
    public void stop() {
        // No thread, suckas
    }

    @Override
    public void timeUpdate() {
        // No need to do things here, either
    }

    @Override
    public void onMoveDecided(MoveRecord record) {
        // Called by ClientServerConnection
        mCallback.onMoveDecided(this, record);
    }

    @Override
    public void setCallback(PlayerCallback callback) {
        mCallback = callback;
    }

    @Override
    public Type getType() {
        return Type.NETWORK_CLIENT;
    }
}
