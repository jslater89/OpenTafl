package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.fetlar.Fetlar;
import com.manywords.softworks.tafl.ui.command.CommandResult;
import com.manywords.softworks.tafl.ui.player.Player;

class CornerVictoryTest extends TaflTest implements UiCallback {

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
        // TODO Auto-generated method stub

    }

    @Override
    public void victoryForSide(Side side) {
        // TODO Auto-generated method stub

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
        Rules rules = Fetlar.newFetlarTest();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();
        state.setCurrentSide(state.getDefenders());

        state.moveTaflman(state.getPieceAt(0, 2), state.getSpaceAt(0, 0));
        state = game.getCurrentState();

        assert state.checkVictory() == GameState.DEFENDER_WIN;
        assert state.getBoard().getOccupier(5, 5) == Taflman.EMPTY;
    }

}
