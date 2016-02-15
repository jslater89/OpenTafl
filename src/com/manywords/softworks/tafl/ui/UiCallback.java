package com.manywords.softworks.tafl.ui;

import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Side;

public interface UiCallback {
    /*
     * These functions are for the game engine to report to the UI.
     */

    public void gameStateAdvanced();
    public void victoryForSide(Side side);

    /*
     * These functions are for the players to request the UI do things.
     */
    public MoveRecord waitForHumanMoveInput();
}
