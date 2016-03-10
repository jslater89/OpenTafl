package com.manywords.softworks.tafl.ui.player;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.ui.player.external.ExternalEngineHost;

import java.io.File;

/**
 * Created by jay on 3/10/16.
 */
public class ExternalEnginePlayer extends Player {
    private MoveCallback mCallback;
    private ExternalEngineHost mHost;

    public void setupEngine(File iniFile) {
        mHost = new ExternalEngineHost(iniFile);
    }

    @Override
    public void getNextMove(UiCallback ui, Game game, int thinkTime) {

    }

    @Override
    public void stop() {

    }

    @Override
    public void onMoveDecided(MoveRecord record) {
        mCallback.onMoveDecided(this, record);
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
