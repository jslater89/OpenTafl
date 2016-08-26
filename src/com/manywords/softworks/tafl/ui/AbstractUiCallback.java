package com.manywords.softworks.tafl.ui;

import com.manywords.softworks.tafl.command.CommandResult;
import com.manywords.softworks.tafl.command.player.Player;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Side;

/**
 * Created by jay on 8/18/16.
 */
public abstract class AbstractUiCallback implements UiCallback {
    @Override
    public void gameStarting() {

    }

    @Override
    public void modeChanging(Mode mode, Object gameObject) {

    }

    @Override
    public void awaitingMove(Player player, boolean isAttackingSide) {

    }

    @Override
    public void timeUpdate(boolean currentSideAttackers) {

    }

    @Override
    public void moveResult(CommandResult result, MoveRecord move) {

    }

    @Override
    public void statusText(String text) {

    }

    @Override
    public void modalStatus(String title, String text) {

    }

    @Override
    public void gameStateAdvanced() {

    }

    @Override
    public void victoryForSide(Side side) {

    }

    @Override
    public void gameFinished() {

    }

    @Override
    public MoveRecord waitForHumanMoveInput() {
        return null;
    }

    @Override
    public boolean inGame() {
        return false;
    }
}
