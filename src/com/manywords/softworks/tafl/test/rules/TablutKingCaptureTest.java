package com.manywords.softworks.tafl.test.rules;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.Taflman;
import com.manywords.softworks.tafl.rules.tablut.Tablut;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.command.CommandResult;
import com.manywords.softworks.tafl.command.player.Player;

public class TablutKingCaptureTest extends TaflTest implements UiCallback {

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
        Rules rules = Tablut.newCenterKingCaptureTest();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get(6,4), Coord.get(5,4)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        assert game.getCurrentState().getPieceAt(4, 4) != Taflman.EMPTY;
        assert game.getCurrentState().checkVictory() == GameState.GOOD_MOVE;

        state.makeMove(new MoveRecord(Coord.get(5,5), Coord.get(5,8)));
        state = game.getCurrentState();

        state.makeMove(new MoveRecord(Coord.get(4,2), Coord.get(4,3)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        assert game.getCurrentState().getPieceAt(4, 4) == Taflman.EMPTY;
        assert game.getCurrentState().checkVictory() == GameState.ATTACKER_WIN;

        rules = Tablut.newCenterKingCaptureTest();
        game = new Game(rules, null);
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get(6,4), Coord.get(6,3)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get(3,3), Coord.get(3,0)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get(3,4), Coord.get(3,3)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get(5,3), Coord.get(5,0)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get(4,5), Coord.get(4,7)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get(4,4), Coord.get(4,3)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get(4,2), Coord.get(4,1)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get(5,5), Coord.get(7,5)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get(6,3), Coord.get(5,3)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        assert game.getCurrentState().getPieceAt(4, 3) != Taflman.EMPTY;
        assert game.getCurrentState().checkVictory() == GameState.GOOD_MOVE;

        state.makeMove(new MoveRecord(Coord.get(7,5), Coord.get(6,5)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get(4,1), Coord.get(4,2)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        assert game.getCurrentState().getPieceAt(4, 3) == Taflman.EMPTY;
        assert game.getCurrentState().checkVictory() == GameState.ATTACKER_WIN;

        rules = Tablut.newCenterKingCaptureTest();
        game = new Game(rules, null);
        state = game.getCurrentState();

        state.makeMove(new MoveRecord(Coord.get(4,2), Coord.get(4,0)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get(4,4), Coord.get(4,1)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        state.makeMove(new MoveRecord(Coord.get(4,5), Coord.get(4,2)));
        state = game.getCurrentState();
        //RawTerminal.renderGameState(state);

        assert game.getCurrentState().getPieceAt(4, 1) == Taflman.EMPTY;
        assert game.getCurrentState().checkVictory() == GameState.ATTACKER_WIN;
    }

}
