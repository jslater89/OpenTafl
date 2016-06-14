package com.manywords.softworks.tafl.ui;

import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.command.CommandResult;
import com.manywords.softworks.tafl.command.player.Player;

public interface UiCallback {
    public enum Mode {
        GAME,
        REPLAY
    }
    /*
     * These functions are for the game engine to report to the UI.
     */

    public void gameStarting();

    /**
     * Sent when the game mode changes between real game and replay.
     *
     * @param mode
     * @param gameObject If mode is GAME, a Game object; otherwise a ReplayGame object.
     */
    public void modeChanging(Mode mode, Object gameObject);
    public void awaitingMove(Player player, boolean isAttackingSide);
    public void timeUpdate(boolean currentSideAttackers);
    public void moveResult(CommandResult result, MoveRecord move);
    public void statusText(String text);
    public void modalStatus(String title, String text);
    public void gameStateAdvanced();
    public void victoryForSide(Side side);
    public void gameFinished();

    /*
     * These functions are for the players to request the UI do things.
     */
    public MoveRecord waitForHumanMoveInput();
    public boolean inGame();
}
