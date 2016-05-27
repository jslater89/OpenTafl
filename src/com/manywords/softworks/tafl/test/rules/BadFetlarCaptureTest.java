package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.fetlar.Fetlar;
import com.manywords.softworks.tafl.command.CommandResult;
import com.manywords.softworks.tafl.command.player.Player;

public class BadFetlarCaptureTest extends TaflTest implements UiCallback {

    @Override
    public void gameStarting() {

    }

    @Override
    public void modeChanging(Mode mode, Object gameObject) {

    }

    @Override
    public void awaitingMove(Player currentPlayer, boolean isAttackingSide) {

    }

    @Override
    public void timeUpdate(Side side) {

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

    @Override
    public void run() {
        Rules rules = Fetlar.newFetlar11();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();

        char ofInterest = state.getPieceAt(3, 5);
        state.moveTaflman(ofInterest, Coord.get(2, 5));
        state = game.getCurrentState();

        assert state.getPieceAt(1, 5) != Taflman.EMPTY;
    }

}
