package com.manywords.softworks.tafl.ui.player;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.ui.RawTerminal;
import com.manywords.softworks.tafl.ui.UiCallback;

public interface Player {
    public enum Type {
        HUMAN,
        NETWORK,
        AI,
        ENGINE
    }

    public interface MoveCallback {
        public void onMoveDecided(MoveRecord record);
    }

    public void getNextMove(UiCallback ui, Game game, int searchDepth);
    public void stop();
    public void onMoveDecided(MoveRecord record);
    public void setCallback(MoveCallback callback);

    public Type getType();
}
