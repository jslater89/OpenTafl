package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.tablut.Tablut;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.command.CommandResult;
import com.manywords.softworks.tafl.command.player.Player;

class TablutKingCaptureTest extends TaflTest implements UiCallback {

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
        Rules rules = Tablut.newCenterKingCaptureTest();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.moveTaflman(state.getPieceAt(6, 4), state.getSpaceAt(5, 4));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        assert game.getCurrentState().getPieceAt(4, 4) != Taflman.EMPTY;
        assert game.getCurrentState().checkVictory() == GameState.GOOD_MOVE;

        state.moveTaflman(state.getPieceAt(5, 5), state.getSpaceAt(5, 8));
        state = game.getCurrentState();

        state.moveTaflman(state.getPieceAt(4, 2), state.getSpaceAt(4, 3));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        assert game.getCurrentState().getPieceAt(4, 4) == Taflman.EMPTY;
        assert game.getCurrentState().checkVictory() == GameState.ATTACKER_WIN;

        rules = Tablut.newCenterKingCaptureTest();
        game = new Game(rules, null);
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.moveTaflman(state.getPieceAt(6, 4), state.getSpaceAt(6, 3));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.moveTaflman(state.getPieceAt(3, 3), state.getSpaceAt(3, 0));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.moveTaflman(state.getPieceAt(3, 4), state.getSpaceAt(3, 3));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.moveTaflman(state.getPieceAt(5, 3), state.getSpaceAt(5, 0));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.moveTaflman(state.getPieceAt(4, 5), state.getSpaceAt(4, 7));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.moveTaflman(state.getPieceAt(4, 4), state.getSpaceAt(4, 3));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.moveTaflman(state.getPieceAt(4, 2), state.getSpaceAt(4, 1));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.moveTaflman(state.getPieceAt(5, 5), state.getSpaceAt(7, 5));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.moveTaflman(state.getPieceAt(6, 3), state.getSpaceAt(5, 3));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        assert game.getCurrentState().getPieceAt(4, 3) != Taflman.EMPTY;
        assert game.getCurrentState().checkVictory() == GameState.GOOD_MOVE;

        state.moveTaflman(state.getPieceAt(7, 5), state.getSpaceAt(6, 5));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.moveTaflman(state.getPieceAt(4, 1), state.getSpaceAt(4, 2));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        assert game.getCurrentState().getPieceAt(4, 3) == Taflman.EMPTY;
        assert game.getCurrentState().checkVictory() == GameState.ATTACKER_WIN;

        rules = Tablut.newCenterKingCaptureTest();
        game = new Game(rules, null);
        state = game.getCurrentState();

        state.moveTaflman(state.getPieceAt(4, 2), state.getSpaceAt(4, 0));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.moveTaflman(state.getPieceAt(4, 4), state.getSpaceAt(4, 1));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.moveTaflman(state.getPieceAt(4, 5), state.getSpaceAt(4, 2));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        assert game.getCurrentState().getPieceAt(4, 1) == Taflman.EMPTY;
        assert game.getCurrentState().checkVictory() == GameState.ATTACKER_WIN;
    }

}
