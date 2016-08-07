package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.seabattle.SeaBattle;
import com.manywords.softworks.tafl.command.CommandResult;
import com.manywords.softworks.tafl.command.player.Player;

public class EdgeVictoryTest extends TaflTest implements UiCallback {

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

    @Override
    public void run() {
        Rules rules = SeaBattle.newStrongKingTest();
        Game game = new Game(rules, null);

        GameState state = game.getCurrentState();
        state.makeMove(new MoveRecord(Coord.get(4,5), Coord.get(4,8)));

        state = game.getCurrentState();
        state.makeMove(new MoveRecord(Coord.get(4,4), Coord.get(4,7)));

        state = game.getCurrentState();
        state.makeMove(new MoveRecord(Coord.get(4,8), Coord.get(5,8)));

        state = game.getCurrentState();
        state.makeMove(new MoveRecord(Coord.get(4,7), Coord.get(0,7)));

        state = game.getCurrentState();
        assert state.checkVictory() == GameState.DEFENDER_WIN;

        assert state.getBoard().getOccupier(4, 4) == Taflman.EMPTY;
    }

}
