package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.seabattle.SeaBattle;
import com.manywords.softworks.tafl.ui.command.CommandResult;
import com.manywords.softworks.tafl.ui.player.Player;

class StrongKingCaptureTest extends TaflTest implements UiCallback {

    @Override
    public void gameStarting() {

    }

    @Override
    public void awaitingMove(Player currentPlayer, boolean isAttackingSide) {

    }

    @Override
    public void moveResult(CommandResult result, MoveRecord move) {

    }

    @Override
    public void statusText(String text) {

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
        Rules rules = SeaBattle.newStrongKingTest();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.moveTaflman(state.getPieceAt(6, 4), state.getSpaceAt(5, 4));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        assert game.getCurrentState().getPieceAt(4, 4) == Taflman.EMPTY;
        assert game.getCurrentState().checkVictory() == GameState.ATTACKER_WIN;
    }

}
