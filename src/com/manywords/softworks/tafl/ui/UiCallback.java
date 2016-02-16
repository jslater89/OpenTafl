package com.manywords.softworks.tafl.ui;

import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.ui.command.CommandResult;

public interface UiCallback {
    /*
     * These functions are for the game engine to report to the UI.
     */

    public void gameStarting();
    public void awaitingMove(boolean isAttackingSide);
    public void moveResult(CommandResult result, MoveRecord move);
    public void statusText(String text);
    public void gameStateAdvanced();
    public void victoryForSide(Side side);
    public void gameFinished();

    /*
     * These functions are for the players to request the UI do things.
     */
    public MoveRecord waitForHumanMoveInput();
    public boolean inGame();
}
