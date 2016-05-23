package com.manywords.softworks.tafl.command.player.external.network;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.command.player.Player;
import com.manywords.softworks.tafl.command.player.external.network.server.NetworkServer;

/**
 * Created by jay on 5/22/16.
 */
public class NetworkPlayer extends Player {
    private NetworkServer mServer;
    private PlayerCallback mCallback;

    @Override
    public void getNextMove(UiCallback ui, Game game, int thinkTime) {

    }

    @Override
    public void moveResult(int moveResult) {

    }

    @Override
    public void opponentMove(MoveRecord move) {

    }

    @Override
    public void stop() {

    }

    @Override
    public void timeUpdate() {

    }

    @Override
    public void onMoveDecided(MoveRecord record) {
        //mServer.sendMove();
    }

    @Override
    public void setCallback(PlayerCallback callback) {
        mCallback = callback;
    }

    @Override
    public Type getType() {
        return Type.NETWORK;
    }
}
