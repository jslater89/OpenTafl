package com.manywords.softworks.tafl.test.consistency;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.GameState;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Coord;
import com.manywords.softworks.tafl.rules.Rules;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.rules.berserk.Berserk;
import com.manywords.softworks.tafl.test.TaflTest;
import com.manywords.softworks.tafl.ui.UiCallback;
import com.manywords.softworks.tafl.command.CommandResult;
import com.manywords.softworks.tafl.command.player.Player;

public class BerserkHistoryDuplicationTest extends TaflTest implements UiCallback {

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
        Rules rules = Berserk.newBerserk11();
        Game game = new Game(rules, null);
        GameState state = game.getCurrentState();

        long lastZobrist = state.mZobristHash;

        //f4-i4
        MoveRecord move = new MoveRecord(Coord.get(5,3), Coord.get(8,3));
        state.makeMove(move);
        state = game.getCurrentState();

        assert state.mZobristHash != lastZobrist;
        lastZobrist = state.mZobristHash;

        //h1-h3
        move = new MoveRecord(Coord.get(7,0), Coord.get(7,2));
        assert GameState.GOOD_MOVE == state.makeMove(move);
        state = game.getCurrentState();

        assert state.mZobristHash != lastZobrist;
        lastZobrist = state.mZobristHash;

        //h6-i6
        move = new MoveRecord(Coord.get(7,5), Coord.get(8,5));
        state.makeMove(move);
        state = game.getCurrentState();

        assert state.mZobristHash != lastZobrist;
        lastZobrist = state.mZobristHash;

        //k4-j4
        move = new MoveRecord(Coord.get(10,3), Coord.get(9,3));
        state.makeMove(move);
        state = game.getCurrentState();

        assert state.mZobristHash != lastZobrist;
        lastZobrist = state.mZobristHash;

        //d6-d5
        move = new MoveRecord(Coord.get(3,5), Coord.get(3,4));
        state.makeMove(move);
        state = game.getCurrentState();

        assert state.mZobristHash != lastZobrist;
        lastZobrist = state.mZobristHash;

        // h3-h4
        move = new MoveRecord(Coord.get(7,2), Coord.get(7,3));
        state.makeMove(move);
        state = game.getCurrentState();

        assert state.mZobristHash != lastZobrist;
        lastZobrist = state.mZobristHash;

        // h4-h6
        move = new MoveRecord(Coord.get(7,3), Coord.get(7,5));
        state.makeMove(move);
        state = game.getCurrentState();

        assert state.mZobristHash != lastZobrist;
        lastZobrist = state.mZobristHash;

        assert state.getLastMoveResult() == GameState.GOOD_MOVE;
    }

}
