package com.manywords.softworks.tafl.ui.player;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.ui.RawTerminal;

public interface Player {
    public enum Type {
        HUMAN,
        NETWORK,
        AI
    }

    public MoveRecord getNextMove(RawTerminal ui, Game game, int searchDepth);

    public Type getType();
}
