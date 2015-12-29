package com.manywords.softworks.tafl.ui.player;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.ui.RawTerminal;

public class LocalHuman implements Player {

    @Override
    public MoveRecord getNextMove(RawTerminal ui, Game game, int searchDepth) {
        System.out.println("Waiting for human move.");
        MoveRecord r = null;

        while (ui.inGame()) {
            r = ui.waitForInGameInput();
            if (r != null) return r;
        }

        return null;
    }

    @Override
    public Type getType() {
        return Type.HUMAN;
    }
}
