package com.manywords.softworks.tafl.ui.player;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.ui.player.external.engine.ExternalEngineHost;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by jay on 3/10/16.
 */
public class ExternalEnginePlayer extends Player {
    private MoveCallback mCallback;
    private ExternalEngineHost mHost;
    private MoveRecord mMyLastMove;

    public void setupEngine(File iniFile) {
        mHost = new ExternalEngineHost(this, iniFile);
        mMyLastMove = null;
        if(getGame() == null) throw new IllegalStateException("ExternalEnginePlayer game is null when setting up engine!");
        mHost.setGame(getGame());
    }

    public ExternalEngineHost getExternalEngineHost() {
        return mHost;
    }

    @Override
    public void setGame(Game game) {
        super.setGame(game);
    }

    @Override
    public void getNextMove(UiCallback ui, Game game, int thinkTime) {
        // Support berserk...
        List<MoveRecord> movesSinceMyLastMove = new ArrayList<>(1);

        int historySize = game.getHistory().size();
        for(int i = historySize - 1; i >= 0; i--) {
            GameState s = game.getHistory().get(i);
            if(s.getEnteringMove().equals(mMyLastMove)) movesSinceMyLastMove.add(s.getEnteringMove());
            else break;
        }

        Collections.reverse(movesSinceMyLastMove);
        mHost.notifyMovesMade(movesSinceMyLastMove);
        mHost.playForCurrentSide(game);
    }

    @Override
    public void stop() {

    }

    @Override
    public void onMoveDecided(MoveRecord record) {
        mCallback.onMoveDecided(this, record);
        mMyLastMove = record;
    }

    @Override
    public void setCallback(MoveCallback callback) {
        mCallback = callback;
    }

    @Override
    public Type getType() {
        return null;
    }
}
