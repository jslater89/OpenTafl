package com.manywords.softworks.tafl.ui.player;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.ui.RawTerminal;
import com.manywords.softworks.tafl.ui.UiCallback;

public abstract class Player {
    public enum Type {
        HUMAN,
        NETWORK,
        AI,
        ENGINE
    }

    public interface MoveCallback {
        public void onMoveDecided(Player player, MoveRecord record);
    }

    public abstract void getNextMove(UiCallback ui, Game game, int searchDepth);
    public abstract void stop();
    public abstract void onMoveDecided(MoveRecord record);
    public abstract void setCallback(MoveCallback callback);

    public abstract Type getType();

    public static Player getNewPlayer(Type type) {
        switch(type) {
            case HUMAN:
                return new LocalHuman();
            case NETWORK:
                return new LocalHuman();
            case AI:
                return new LocalAi();
            case ENGINE:
                return new LocalAi();
        }

        return null;
    }
}
