package com.manywords.softworks.tafl.command.player;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.ui.UiCallback;

/**
 * Created by jay on 6/8/16.
 */
public class DoNothingPlayer extends Player {
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

    }

    @Override
    public void setCallback(PlayerCallback callback) {

    }

    @Override
    public Type getType() {
        return Type.NETWORK_CLIENT;
    }
}
