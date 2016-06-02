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
        // The role of the player over the network is 'out of game', if we're out of game somehow, or the role we are not.
        mGameRole = (c.getGameRole() == GameRole.OUT_OF_GAME ? GameRole.OUT_OF_GAME : (c.getGameRole() == GameRole.ATTACKER ? GameRole.DEFENDER : GameRole.ATTACKER));
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
        // i.e., the local player has decided on a move.
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
        // i.e., the network player has decided upon a move. ClientServerConnection calls here.
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
